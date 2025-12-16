package modexplorer.classexplorers;

import org.objectweb.asm.*;
import modexplorer.Main;

import java.util.*;

/**
 * Points to all the call sites of Enum.values()
 * <p>
 * This method performs an Array.clone() operation everytime it is called which can cause a lot
 * of memory utilisation when called often on large enums
 */
public class EnumValuesCallsitesFinder extends ClassVisitor implements ClassExplorer {

    private static final List<Enumm> enums = new ArrayList<>();
    private static final List<CallSite> callsites = new ArrayList<>();

    private final String fileName;
    private String classname;
    private boolean isEnum;
    private int enumFields;

    public EnumValuesCallsitesFinder() {
        super(Opcodes.ASM9);
        this.fileName = null;
    }

    public EnumValuesCallsitesFinder(String fileName) {
        super(Opcodes.ASM9);
        this.fileName = fileName;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.classname = name;
        if ("java/lang/Enum".equals(superName)) {
            this.isEnum = true;
        }
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (this.isEnum && access == Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL + Opcodes.ACC_ENUM && desc.equals("L" + this.classname + ";")) {
            this.enumFields++;
        }
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new EnumMethodVisitor(name, desc);
    }

    @Override
    public void visitEnd() {
        if (isEnum) {
            enums.add(new Enumm(this.classname, enumFields));
        }
    }

    private String getClassLocation() {
        return this.fileName + "/" + this.classname;
    }

    class EnumMethodVisitor extends MethodVisitor {

        private final String methodName;
        private final String desc;

        public EnumMethodVisitor(String methodName, String desc) {
            super(Opcodes.ASM9);
            this.methodName = methodName;
            this.desc = desc;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == Opcodes.INVOKESTATIC && name.equals("values") && desc.equals("()[L" + owner + ";")) {
                callsites.add(new CallSite(getClassLocation() + ";" + this.methodName + this.desc, owner));
            }
        }

    }

    static class Enumm implements Comparable<Enumm> {
        private final String type;
        private final int size;

        Enumm(String type, int size) {
            this.type = type;
            this.size = size;
        }

        @Override
        public int compareTo(Enumm o) {
            final int compareInt = Integer.compare(o.size, this.size);
            if (compareInt == 0) {
                return this.type.compareTo(o.type);
            }
            return compareInt;
        }
    }

    static class CallSite implements Comparable<CallSite> {
        private final String callsite;
        private final String enumType;// without the L ;

        CallSite(String callsite, String enumType) {
            this.callsite = callsite;
            this.enumType = enumType;
        }

        @Override
        public int compareTo(CallSite o) {
            return this.enumType.compareTo(o.enumType);
        }
    }

    @Override
    public void visitClass(ClassReader cr, String fileName) {
        cr.accept(new EnumValuesCallsitesFinder(fileName), ClassReader.SKIP_DEBUG);
    }

    @Override
    public void onSearchEnd() {
        enums.sort(null);
        Map<String, List<CallSite>> callSiteMap = new HashMap<>();
        for (CallSite site : callsites) {
            callSiteMap.computeIfAbsent(site.enumType, k -> new ArrayList<>()).add(site);
        }
        String latestLine = "";
        for (Enumm anEnum : enums) {
            final List<CallSite> list = callSiteMap.get(anEnum.type);
            if (list != null) {
                for (CallSite site : list) {
                    final String message = "Size : " + anEnum.size + " Enum : " + anEnum.type + " at " + site.callsite;
                    if (!latestLine.equals(message)) {
                        Main.log(message);
                    }
                    latestLine = message;
                    callsites.remove(site);
                }
            }
        }
        callsites.sort(null);
        for (CallSite site : callsites) {
            final String message = "Size : ? Enum : " + site.enumType + " at " + site.callsite;
            if (!latestLine.equals(message)) {
                Main.log(message);
            }
            latestLine = message;
        }
    }

}
