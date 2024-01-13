package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 * <p/>
 * All rights reserved.
 * <p/>
 * File Created at 11:31:12 on 02-Feb-2006
 */

public final class ImportOptions extends ImportStatement {
   public final Option options;

   public ImportOptions(Option options, SourcePosition thePosition) {
      super(thePosition);
      this.options = options;
   }

   public final Object visit(Visitor v, Object o) {
      return v.visitImportOptions(this, o);
   }
}

