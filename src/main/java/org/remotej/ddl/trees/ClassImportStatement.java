package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;


public class ClassImportStatement extends Statement {

    public Identifier importStatement;

    public ClassImportStatement(SourcePosition pos) {
        super(pos);
    }

    public ClassImportStatement(Identifier s, SourcePosition thePosition) {
        super(thePosition);
        this.importStatement = s;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitClassImportStatement(this, o);
    }

}
