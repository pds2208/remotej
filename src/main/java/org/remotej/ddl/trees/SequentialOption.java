package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 * <p/>
 * All rights reserved.
 * <p/>
 * File Created at 13:52:54 on 31-Jan-2006
 */

public final class SequentialOption extends Option {

   public final Option opt1;
   public final Option opt2;

   public SequentialOption(Option opt1, Option opt2, SourcePosition thePosition) {
      super(thePosition);
      this.opt1 = opt1;
      this.opt2 = opt2;
   }

   public final Object visit(Visitor v, Object o) {
      return v.visitSequentialOption(this, o);
   }
}
