package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class Identifier extends Terminal {

    public Identifier(String theSpelling, SourcePosition thePosition) {
        super(theSpelling, thePosition);
    }


    public final Object visit(Visitor v, Object o) {
        return v.visitIdentifier(this, o);
    }

}
