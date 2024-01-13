package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class ReturnValue extends Statement {
    public Identifier returnValue;
    public Parameter.CALL_TYPE type;

    public ReturnValue(Identifier returnValue, Parameter.CALL_TYPE type, SourcePosition thePosition) {
        super(thePosition);
        this.returnValue = returnValue;
        this.type = type;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitReturnValue(this, o);
    }
}
