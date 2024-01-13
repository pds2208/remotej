package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class ImportDeclaration extends ImportStatement {

    public final ClassValue aClass;

    public ImportDeclaration(ClassValue aClass, SourcePosition thePosition) {
        super(thePosition);
        this.aClass = aClass;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitImportDeclaration(this, o);
    }
}
