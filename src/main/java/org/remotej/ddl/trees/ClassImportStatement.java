package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright(c) Paul Soule. All rights reserved.
 * <p/>
 * This file is part of the RemoteJ system.
 * <p/>
 * Created at 2:37:34 PM on Apr 15, 2006
 */
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
