package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class SequentialPointcutStatement extends PointcutStatement {

    public final PointcutStatement st1;
    public final PointcutStatement st2;

    public SequentialPointcutStatement(PointcutStatement st1, PointcutStatement st2, SourcePosition thePosition) {
        super(thePosition);
        this.st1 = st1;
        this.st2 = st2;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitSequentialPointcutStatement(this, o);
    }

}
