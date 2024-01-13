package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class SequentialClassImportStatement extends ClassImportStatement {

    public final ClassImportStatement st1;
    public final ClassImportStatement st2;

    public SequentialClassImportStatement(ClassImportStatement is, ClassImportStatement ci, SourcePosition pos) {
        super(pos);
        st1 = is;
        st2 = ci;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitSequentialClassImportStatement(this, o);
    }
}
