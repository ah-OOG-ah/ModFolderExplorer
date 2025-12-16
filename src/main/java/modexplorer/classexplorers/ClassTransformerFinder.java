package modexplorer.classexplorers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import modexplorer.Main;

public class ClassTransformerFinder extends ClassVisitor implements ClassExplorer {

    private String fileName;
    private String classname;

    public ClassTransformerFinder() {
        super(Opcodes.ASM9);
    }

    public ClassTransformerFinder(String fileName) {
        super(Opcodes.ASM9);
        this.fileName = fileName;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.classname = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals("transform") && desc.equals("(Ljava/lang/String;Ljava/lang/String;[B)[B")) {
            log();
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitClass(ClassReader cr, String fileName) {
        cr.accept(new ClassTransformerFinder(fileName), ClassReader.SKIP_DEBUG);
    }

    @Override
    public void onSearchEnd() {}

    private void log() {
        Main.log(this.fileName + "/" + this.classname);
    }

}
