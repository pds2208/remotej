package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 * <p/>
 * All rights reserved.
 * <p/>
 * File Created at 14:08:27 on 23-Jan-2006
 */

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
