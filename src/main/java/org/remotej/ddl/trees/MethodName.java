package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class MethodName extends Statement {
    public final Identifier methodValue;

    public MethodName(Identifier methodValue, SourcePosition thePosition) {
        super(thePosition);
        this.methodValue = methodValue;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitMethodName(this, o);
    }
}
