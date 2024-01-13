package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright(c) Paul Soule. All rights reserved.
 * <p/>
 * This file is part of the RemoteJ system.
 * <p/>
 * Created at 9:42:38 PM on Apr 13, 2006
 */
public final class BooleanValue extends Terminal {

   public BooleanValue(String theSpelling, SourcePosition thePosition) {
      super(theSpelling, thePosition);
   }


   public final Object visit(Visitor v, Object o) {
      return v.visitBooleanValue(this, o);
   }

}

