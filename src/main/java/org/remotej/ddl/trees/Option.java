package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public abstract class Option extends Statement {

    public Option(SourcePosition thePosition) {
        super(thePosition);
    }

}
