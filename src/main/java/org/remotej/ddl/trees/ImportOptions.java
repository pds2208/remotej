package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;


public final class ImportOptions extends ImportStatement {
    public final Option options;

    public ImportOptions(Option options, SourcePosition thePosition) {
        super(thePosition);
        this.options = options;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitImportOptions(this, o);
    }
}
