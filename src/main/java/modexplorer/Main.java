package modexplorer;

import modexplorer.classexplorers.ClassExplorer;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class Main {

    private static PrintStream printStream;
    private static final Pattern zipJarFilePattern = Pattern.compile("(.+)\\.(zip|jar)$");
    private static final Pattern classFilePattern = Pattern.compile("(.+)\\.class$");
    private static final Pattern classPattern = Pattern.compile("[^\\s$]+(\\$\\S+)?\\.class$");
    private static final List<ClassExplorer> classExplorers = new ArrayList<>();

    /**
     * args[] should contain paths to folders containing .jar or .class files to explore.
     * For example :
     * - args[0] = "C:/MultiMC/instances/GTNH/.minecraft/mods"
     * - args[1] = "D:/MinecraftStuff/MinecraftSource"
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            throw new RuntimeException("Mod folder path is invalid");
        }
        final List<File> modFileList = new ArrayList<>();
        final List<File> classFileList = new ArrayList<>();
        for (String path : args) {
            final File folder = new File(path);
            if (!folder.exists()) {
                throw new RuntimeException("Folder path is invalid : " + path);
            }
            fillFileLists(folder, modFileList, classFileList);
        }
        System.out.println("Identified " + modFileList.size() + " .jar files and " + classFileList.size() + " .class files");
        setupLogger();
        registerExplorers();
        exploreFiles(modFileList, classFileList);
        onSearchEnd();
        if (printStream != null) printStream.close();
    }

    private static void setupLogger() {
        try {
            final File logFile = new File(System.getProperty("user.dir") + "/run", "output" + ".txt");
            if (logFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                logFile.delete();
            }
            if (!logFile.exists()) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    logFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't create log file");
                }
            }
            printStream = new PrintStream(new FileOutputStream(logFile, true));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Couldn't create log file");
        }
    }

    private static void registerExplorers() {
        // TODO register class explorers here
        // classExplorers.add(new ClassTransformerFinder());
        // classExplorers.add(new EnumValuesCallsitesFinder());
        // classExplorers.add(new GLEnabledDisabledTracker());
        // classExplorers.add(new SubscribeEventFinder());
    }

    public static void log(String message) {
        printStream.println(message);
    }

    private static void fillFileLists(File folder, List<File> jarFiles, List<File> classFiles) {
        final File[] fList = folder.listFiles();
        if (fList != null) {
            for (final File file : fList) {
                if (file.isFile()) {
                    if (zipJarFilePattern.matcher(file.getName()).matches()) {
                        jarFiles.add(file);
                    } else if (classFilePattern.matcher(file.getName()).matches()) {
                        classFiles.add(file);
                    }
                } else if (file.isDirectory()) {
                    fillFileLists(file, jarFiles, classFiles);
                }
            }
        }
    }

    private static void exploreFiles(List<File> jarFiles, List<File> classFiles) {
        final long time = System.currentTimeMillis();
        int classCount = 0;
        for (final File file : jarFiles) {
            try (final JarFile jar = new JarFile(file)) {
                for (final ZipEntry ze : Collections.list(jar.entries())) {
                    final String classFileName = ze.getName();
                    if (classPattern.matcher(classFileName).matches()) {
                        try (final InputStream inputStream = jar.getInputStream(ze)) {
                            exploreClass(readClass(inputStream), file.getName(), classFileName);
                            classCount++;
                        } catch (Exception e) {
                            System.out.println("There was an error attempting to parse " + file + "/" + ze);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading " + file);
            }
        }
        for (File file : classFiles) {
            try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                exploreClass(readClass(inputStream), "", file.getPath());
                classCount++;
            } catch (IOException e) {
                System.out.println("Error reading " + file);
            }
        }
        System.out.println("Visited " + classCount + " classes in " + (System.currentTimeMillis() - time) + "ms");
    }

    //final static ClassConstantPoolParser cstParser = new ClassConstantPoolParser(
    //        "com/cleanroommc/bogosorter/api/ISlot"
    //);

    private static void exploreClass(byte[] classBytes, String jarFileName, String classFileName) {
        //if (cstParser.find(classBytes)) {
        //    log(classFileName);
        //}
        final ClassReader classReader = new ClassReader(classBytes);
        for (ClassExplorer explorer : classExplorers) {
            explorer.visitClass(classReader, jarFileName);
        }

        //classReader.accept(new ModClassVisitor(jarFileName), ClassReader.SKIP_DEBUG);

        //ClassNode cn = new ClassNode();
        //classReader.accept(cn, 0);
    }

    private static void onSearchEnd() {
        for (ClassExplorer explorer : classExplorers) {
            explorer.onSearchEnd();
        }
    }

    private static byte[] readClass(InputStream is) throws IOException {
        if (is == null) {
            throw new IOException("Class not found");
        } else {
            byte[] var2 = new byte[is.available()];
            int var3 = 0;

            while (true) {
                int var4 = is.read(var2, var3, var2.length - var3);
                if (var4 == -1) {
                    byte[] var10;
                    if (var3 < var2.length) {
                        var10 = new byte[var3];
                        System.arraycopy(var2, 0, var10, 0, var3);
                        var2 = var10;
                    }

                    var10 = var2;
                    return var10;
                }

                var3 += var4;
                if (var3 == var2.length) {
                    int var5 = is.read();
                    byte[] var6;
                    if (var5 < 0) {
                        var6 = var2;
                        return var6;
                    }

                    var6 = new byte[var2.length + 1000];
                    System.arraycopy(var2, 0, var6, 0, var3);
                    var6[var3++] = (byte) var5;
                    var2 = var6;
                }
            }
        }
    }

}