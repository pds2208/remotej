package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 * <p/>
 * All rights reserved.
 * <p/>
 * File Created at 14:44:33 on 01-Feb-2006
 */

public final class ReturnValue extends Statement {
   public Identifier returnValue;
   public Parameter.CALL_TYPE type;

   public ReturnValue(Identifier returnValue, Parameter.CALL_TYPE type, SourcePosition thePosition) {
      super(thePosition);
      this.returnValue = returnValue;
      this.type = type;
   }

   public final Object visit(Visitor v, Object o) {
      return v.visitReturnValue(this, o);
   }
}
