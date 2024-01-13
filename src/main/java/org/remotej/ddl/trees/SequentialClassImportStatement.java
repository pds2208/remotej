package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright(c) Paul Soule. All rights reserved.
 * <p/>
 * This file is part of the RemoteJ system.
 * <p/>
 * Created at 2:38:26 PM on Apr 15, 2006
 */
public final class SequentialClassImportStatement extends ClassImportStatement {

   public final ClassImportStatement st1;
   public final ClassImportStatement st2;

   public SequentialClassImportStatement(ClassImportStatement is, ClassImportStatement ci, SourcePosition pos) {
      super(pos);
      st1 = is;
      st2 = ci;
   }

   public final Object visit(Visitor v, Object o) {
      return v.visitSequentialClassImportStatement(this, o);
   }
}
