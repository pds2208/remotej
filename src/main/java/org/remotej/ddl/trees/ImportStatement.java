package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;


public abstract class ImportStatement extends Statement {

    public ImportStatement(SourcePosition thePosition) {
        super(thePosition);
    }

}
