package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class ImportMethods extends ImportStatement {
    public final Methods methods;

    public ImportMethods(Methods methods, SourcePosition thePosition) {
        super(thePosition);
        this.methods = methods;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitImportMethods(this, o);
    }
}
