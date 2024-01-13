package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public abstract class TypeDenoter extends AST {

    public TypeDenoter(SourcePosition thePosition) {
        super(thePosition);
    }

    public abstract boolean equals(Object obj);

}
