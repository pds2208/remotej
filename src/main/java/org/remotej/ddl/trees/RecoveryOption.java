package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class RecoveryOption extends MethodOption {

    public Identifier recoveryName;

    public RecoveryOption(Identifier recoveryName, SourcePosition thePosition) {
        super(thePosition);
        this.recoveryName = recoveryName;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitRecoveryOption(this, o);
    }
}
