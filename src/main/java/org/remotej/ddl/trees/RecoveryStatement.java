package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 * <p/>
 * All rights reserved.
 * <p/>
 * File Created at 11:52:01 on 31-Jan-2006
 */

public final class RecoveryStatement extends Statement {
   public final Identifier recoveryName;
   public final Identifier recoveryCode;
   public final ParameterDeclaration parameters;

   public RecoveryStatement(Identifier recoveryName, ParameterDeclaration parameters, Identifier recoveryCode, SourcePosition thePosition) {
      super(thePosition);
      this.recoveryName = recoveryName;
      this.parameters = parameters;
      this.recoveryCode = recoveryCode;
   }

   public final Object visit(Visitor v, Object o) {
      return v.visitRecoveryStatement(this, o);
   }
}
