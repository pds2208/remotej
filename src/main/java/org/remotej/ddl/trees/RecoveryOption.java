package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 * <p/>
 * All rights reserved.
 * <p/>
 * File Created at 13:19:47 on 02-Feb-2006
 */

public final class RecoveryOption extends MethodOption {

   public Identifier recoveryName;

   public RecoveryOption(Identifier recoveryName, SourcePosition thePosition) {
      super(thePosition);
      this.recoveryName = recoveryName;
   }

   public final Object visit(Visitor v, Object o) {
      return v.visitRecoveryOption(this, o);
   }
}
