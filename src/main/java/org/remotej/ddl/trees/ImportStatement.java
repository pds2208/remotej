package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 * <p/>
 * All rights reserved.
 * <p/>
 * File Created at 11:52:32 on 31-Jan-2006
 */

public abstract class ImportStatement extends Statement {

   public ImportStatement(SourcePosition thePosition) {
      super(thePosition);
   }

}
