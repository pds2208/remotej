package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 * <p/>
 * Portions Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * <p/>
 * All rights reserved.
 * <p/>
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 * <p/>
 * File Created at 09:32:35 on 30-Jan-2006
 */

public class ProtocolStatement extends Statement {

   public String protocol;

   public ProtocolStatement(String protocol, SourcePosition thePosition) {
      super(thePosition);
      this.protocol = protocol;
   }

   public final Object visit(Visitor v, Object o) {
      return v.visitProtocolStatement(this, o);
   }

}
