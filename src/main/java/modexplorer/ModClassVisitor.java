package modexplorer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ModClassVisitor extends ClassVisitor {

    private final String fileName;
    private String classname;

    public ModClassVisitor(String fileName) {
        super(Opcodes.ASM9);
        this.fileName = fileName;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.classname = name;
    }

    //@Override
    //public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    //    return new ModAnnotationVisitor();
    //}

    //@Override
    //public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    //    return new ModMethodVisitor(name, desc);
    //}

    private String getClassLocation() {
        return this.fileName + "/" + this.classname;
    }

    class ModMethodVisitor extends MethodVisitor {

        private final String methodName;
        private final String desc;

        public ModMethodVisitor(String methodName, String desc) {
            super(Opcodes.ASM9);
            this.methodName = methodName;
            this.desc = desc;
        }

        //@Override
        //public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        //    if (desc.equals("")) {
        //        log();
        //    }
        //    return null;
        //}

        //@Override
        //public void visitTypeInsn(int opcode, String type) {
        //    if (opcode == Opcodes. && type.equals("")) {
        //        log();
        //    }
        //}

        //@Override
        //public void visitLdcInsn(Object cst) {
        //    if (cst instanceof String) {
        //        log();
        //    }
        //}

        //@Override
        //public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        //    if (owner.equals("") && name.equals("")) {
        //        log("field access " + owner + "." + name + ";" + desc);
        //    }
        //}

        //@Override
        //public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        //    if (owner.equals("") && name.equals("")) {
        //        log("calls " + owner + "." + name + ";" + desc);
        //    }
        //}

        private void log() {
            Main.log(getClassLocation() + ";" + methodName + this.desc);
        }

        private void log(String s) {
            Main.log(getClassLocation() + ";" + this.methodName + this.desc + " " + s);
        }

    }

    static class ModAnnotationVisitor extends AnnotationVisitor {

        public ModAnnotationVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(String name, Object value) {
            System.out.println("visit : name " + name + " value " + value);
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            System.out.println("visitEnum : name " + name + " desc " + desc + " value " + value);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            System.out.println("visitAnnotation : name " + name + " desc " + desc);
            return new ModAnnotationVisitor();
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            System.out.println("visitArray : name " + name);
            return new ModAnnotationVisitor();
        }

    }

}
