package modexplorer.classexplorers;

import org.objectweb.asm.ClassReader;

public interface ClassExplorer {

    void visitClass(ClassReader cr, String fileName);

    void onSearchEnd();

}
