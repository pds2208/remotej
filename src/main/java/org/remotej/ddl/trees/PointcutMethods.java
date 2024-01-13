package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class PointcutMethods extends PointcutStatement {
    public final Methods methods;

    public PointcutMethods(Methods methods, SourcePosition thePosition) {
        super(thePosition);
        this.methods = methods;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitPointcutMethods(this, o);
    }
}
