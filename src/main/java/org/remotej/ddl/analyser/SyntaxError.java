package org.remotej.ddl.analyser;

/**
 * Copyright (c) 2006 Paul Soule
 * Dept. of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
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
 * File Created at 10:09:44 on 16-Jan-2006
 */

@SuppressWarnings("serial")
final class SyntaxError extends Exception {
   public SyntaxError() {
      super();
   }

   public SyntaxError(String s) {
      super(s);
   }

}
