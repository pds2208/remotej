package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 * <p/>
 * All rights reserved.
 * <p/>
 * File Created at 14:06:00 on 23-Jan-2006
 */

public final class IntegerLiteral extends Terminal {

   public IntegerLiteral(String theSpelling, SourcePosition thePosition) {
      super(theSpelling, thePosition);
   }

   public final Object visit(Visitor v, Object o) {
      return v.visitIntegerLiteral(this, o);
   }
}
