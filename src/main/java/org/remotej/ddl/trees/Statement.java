package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;


public abstract class Statement extends AST {

    public Statement(SourcePosition thePosition) {
        super(thePosition);
    }
}
