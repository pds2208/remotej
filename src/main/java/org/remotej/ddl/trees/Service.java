package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public final class Service extends AST {

    public final Identifier serviceName;
    public final Statement service;
    public final ClassImportStatement imports;

    public Service(Identifier svc, ClassImportStatement imports, Statement body, SourcePosition thePosition) {
        super(thePosition);
        this.serviceName = svc;
        this.imports = imports;
        service = body;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitService(this, o);
    }
}
