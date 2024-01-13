package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class PointcutDeclaration extends PointcutStatement {
    public final ClassValue aClass;
    public RecoveryOption recovery = null;

    public PointcutDeclaration(ClassValue aClass, SourcePosition thePosition) {
        super(thePosition);
        this.aClass = aClass;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitPointcutDeclaration(this, o);
    }
}
