package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;


public final class SequentialMethods extends Methods {

    public final Methods opt1;
    public final Methods opt2;

    public SequentialMethods(Methods opt1, Methods opt2, SourcePosition thePosition) {
        super(thePosition);
        this.opt1 = opt1;
        this.opt2 = opt2;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitSequentialMethods(this, o);
    }
}
