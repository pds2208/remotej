package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public class Methods extends Statement {
    public ReturnValue returnValue;
    public MethodName methodName;
    public ParameterDeclaration parameters;
    public MethodOption option;

    public Methods(SourcePosition pos) {
        super(pos);
    }

    public Methods(ReturnValue returnValue, MethodName methodName, ParameterDeclaration parameters,
                   MethodOption option, SourcePosition thePosition) {
        super(thePosition);
        this.returnValue = returnValue;
        this.methodName = methodName;
        this.parameters = parameters;
        this.option = option;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitMethod(this, o);
    }
}
