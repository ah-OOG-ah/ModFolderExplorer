package modexplorer.classexplorers;

import modexplorer.Main;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Finds all the methods annotated with the @SubscribeEvent from forge
 * and prints them organised by event
 */
public class SubscribeEventFinder extends ClassVisitor implements ClassExplorer {

    private static final Map<String, List<String>[]> handlers = new HashMap<>();
    private String fileName;
    private String classname;

    public SubscribeEventFinder() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.classname = name;
    }

    @Override
    public void visitClass(ClassReader cr, String fileName) {
        this.fileName = fileName;
        cr.accept(this, ClassReader.SKIP_DEBUG);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String methodDesc, String signature, String[] exceptions) {
        return new MethodVisitor(Opcodes.ASM9) {
            @Override
            public AnnotationVisitor visitAnnotation(String annotationDesc, boolean visible) {
                if ("Lcpw/mods/fml/common/eventhandler/SubscribeEvent;".equals(annotationDesc)) {
                    String eventDesc = methodDesc.substring(1, methodDesc.indexOf(")"));

                    List<String>[] array = handlers.get(eventDesc);
                    if (array == null) {
                        //noinspection unchecked
                        array = new List[EventPriority.values().length];
                        for (int i = 0; i < array.length; i++) {
                            array[i] = new ArrayList<>();
                        }
                        handlers.put(eventDesc, array);
                    }
                    List<String>[] finalArray = array;

                    return new AnnotationVisitor(Opcodes.ASM9) {
                        private EventPriority priority = EventPriority.NORMAL;

                        @Override
                        public void visitEnum(String name, String descriptor, String value) {
                            if ("priority".equals(name) && "Lcpw/mods/fml/common/eventhandler/EventPriority;".equals(descriptor)) {
                                priority = EventPriority.valueOf(value);
                            }
                        }

                        @Override
                        public void visitEnd() {
                            finalArray[priority.ordinal()].add(fileName + ";" + classname + "." + name);
                        }
                    };
                }
                return super.visitAnnotation(annotationDesc, visible);
            }
        };
    }

    @Override
    public void onSearchEnd() {
        final EventPriority[] VALUES = EventPriority.values();
        final LinkedHashMap<String, List<String>[]> sortedMap = sortByKey(handlers);
        for (Map.Entry<String, List<String>[]> entry : sortedMap.entrySet()) {
            Main.log("-------- " + entry.getKey().substring(1, entry.getKey().length() - 1) + " --------");
            int index = 0;
            final List<String>[] array = entry.getValue();
            for (int i = 0; i < array.length; i++) {
                EventPriority priority = VALUES[i];
                List<String> list = array[i];
                for (String s : list) {
                    Main.log(index + " " + priority.name() + " " + s);
                    index++;
                }
            }
            Main.log(" ");
            Main.log(" ");
        }
    }

    private static <K extends Comparable<K>, V> LinkedHashMap<K, V> sortByKey(Map<K, V> map) {
        final List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByKey());
        final LinkedHashMap<K, V> out = new LinkedHashMap<>();
        for (final Map.Entry<K, V> e : list) {
            out.put(e.getKey(), e.getValue());
        }
        return out;
    }

    enum EventPriority {
        HIGHEST, //First to execute
        HIGH,
        NORMAL,
        LOW,
        LOWEST //Last to execute
    }

}
