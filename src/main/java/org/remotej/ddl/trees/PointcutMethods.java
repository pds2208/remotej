package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 * <p/>
 * All rights reserved.
 * <p/>
 * File Created at 12:23:50 on 31-Jan-2006
 */

public final class PointcutMethods extends PointcutStatement {
   public final Methods methods;

   public PointcutMethods(Methods methods, SourcePosition thePosition) {
      super(thePosition);
      this.methods = methods;
   }

   public final Object visit(Visitor v, Object o) {
      return v.visitPointcutMethods(this, o);
   }
}