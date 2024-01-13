package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright(c) Paul Soule. All rights reserved.
 * <p/>
 * This file is part of the RemoteJ system.
 * <p/>
 * Created at 11:56:32 AM on Mar 5, 2006
 */

public abstract class TypeDenoter extends AST {

   public TypeDenoter(SourcePosition thePosition) {
      super(thePosition);
   }

   public abstract boolean equals(Object obj);

}


