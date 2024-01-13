package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;


public abstract class PointcutStatement extends Statement {

    public PointcutStatement(SourcePosition thePosition) {
        super(thePosition);
    }

}
