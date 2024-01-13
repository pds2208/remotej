package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class SequentialStatement extends Statement {

    public final Statement smt1;
    public final Statement smt2;

    public SequentialStatement(Statement smt1, Statement smt2, SourcePosition thePosition) {
        super(thePosition);
        this.smt1 = smt1;
        this.smt2 = smt2;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitSequentialStatement(this, o);
    }

}
