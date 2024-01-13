package org.remotej.ddl.trees;

public final class Parameter {

    public Identifier type;
    public String referenceType = null;
    public Identifier value;

    public enum CALL_TYPE {REFERENCE, COPY, RESTORE}

    ;
    public CALL_TYPE callType;
    public Object generatedInterface = null;

    public Parameter(CALL_TYPE callType, Identifier type, Identifier value) {
        this.callType = callType;
        this.type = type;
        this.value = value;
    }

    public Parameter(Identifier value) {
        this.type = null;
        this.value = value;
    }
}
