package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

import java.util.LinkedList;

public final class ParameterDeclaration extends Statement {

    /**
     * There are two types of parameters; one where there is a type and a value and the other
     * where there is just a value.
     */

    public final LinkedList<Parameter> parameters = new LinkedList<Parameter>();

    public ParameterDeclaration(SourcePosition thePosition) {
        super(thePosition);
    }

    public final void add(Parameter.CALL_TYPE callType, Identifier type, Identifier value) {
        parameters.add(new Parameter(callType, type, value));
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitParameterDeclaration(this, o);
    }

}
