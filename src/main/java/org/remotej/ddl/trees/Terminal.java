package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public abstract class Terminal extends AST {

    public Terminal(String theSpelling, SourcePosition thePosition) {
        super(thePosition);
        spelling = theSpelling;
    }

    public String spelling;
}
