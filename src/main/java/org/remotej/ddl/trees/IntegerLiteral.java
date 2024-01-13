package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;


public final class IntegerLiteral extends Terminal {

    public IntegerLiteral(String theSpelling, SourcePosition thePosition) {
        super(theSpelling, thePosition);
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitIntegerLiteral(this, o);
    }
}
