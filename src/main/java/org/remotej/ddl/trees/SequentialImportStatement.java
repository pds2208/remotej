package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class SequentialImportStatement extends ImportStatement {

    public final ImportStatement st1;
    public final ImportStatement st2;

    public SequentialImportStatement(ImportStatement st1, ImportStatement st2, SourcePosition thePosition) {
        super(thePosition);
        this.st1 = st1;
        this.st2 = st2;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitSequentialImportStatement(this, o);
    }

}
