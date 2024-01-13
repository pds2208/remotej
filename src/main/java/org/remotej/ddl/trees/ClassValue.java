package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;


public final class ClassValue extends Statement {
    public Identifier className;
    public ReturnValue returnValue;
    public MethodName methodName;
    public ParameterDeclaration parameters;

    public ClassValue(Identifier className, ReturnValue returnValue, MethodName methodName,
                      ParameterDeclaration parameters, SourcePosition thePosition) {
        super(thePosition);
        this.className = className;
        this.returnValue = returnValue;
        this.methodName = methodName;
        this.parameters = parameters;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitClassValue(this, o);
    }
}
