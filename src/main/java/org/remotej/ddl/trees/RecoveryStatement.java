package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;


public final class RecoveryStatement extends Statement {
    public final Identifier recoveryName;
    public final Identifier recoveryCode;
    public final ParameterDeclaration parameters;

    public RecoveryStatement(Identifier recoveryName, ParameterDeclaration parameters, Identifier recoveryCode, SourcePosition thePosition) {
        super(thePosition);
        this.recoveryName = recoveryName;
        this.parameters = parameters;
        this.recoveryCode = recoveryCode;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitRecoveryStatement(this, o);
    }
}
