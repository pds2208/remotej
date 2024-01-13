package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class BooleanValue extends Terminal {

    public BooleanValue(String theSpelling, SourcePosition thePosition) {
        super(theSpelling, thePosition);
    }


    public final Object visit(Visitor v, Object o) {
        return v.visitBooleanValue(this, o);
    }

}
