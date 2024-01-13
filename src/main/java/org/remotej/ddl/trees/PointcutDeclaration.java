package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 * <p/>
 * All rights reserved.
 * <p/>
 * File Created at 15:34:06 on 01-Feb-2006
 */

public final class PointcutDeclaration extends PointcutStatement {
   public final ClassValue aClass;
   public RecoveryOption recovery = null;

   public PointcutDeclaration(ClassValue aClass, SourcePosition thePosition) {
      super(thePosition);
      this.aClass = aClass;
   }

   public final Object visit(Visitor v, Object o) {
      return v.visitPointcutDeclaration(this, o);
   }
}