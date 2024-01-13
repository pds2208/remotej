package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class PointcutOptions extends PointcutStatement {
    public final Option options;

    public PointcutOptions(Option options, SourcePosition thePosition) {
        super(thePosition);
        this.options = options;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitPointcutOptions(this, o);
    }
}
