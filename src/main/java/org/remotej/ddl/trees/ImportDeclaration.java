package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 * <p/>
 * All rights reserved.
 * <p/>
 * File Created at 15:07:30 on 01-Feb-2006
 */

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
