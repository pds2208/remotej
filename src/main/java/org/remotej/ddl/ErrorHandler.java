package org.remotej.ddl;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 *
 * Portions Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 *
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 * <p/>
 * File Created at 09:13:41 on 16-Jan-2006
 */

import org.remotej.ddl.analyser.SourcePosition;

public final class ErrorHandler {

   private int numErrors = 0;
   private int numWarnings = 0;

   public ErrorHandler() {
      numErrors = 0;
      numWarnings = 0;
   }

   public final int getNumErrors() {
      return numErrors;
   }

   public final void setNumErrors(int numErrors) {
      this.numErrors = numErrors;
   }

   public final int getNumWarnings() {
      return numWarnings;
   }

   public final void setNumWarnings(int numWarnings) {
      this.numWarnings = numWarnings;
   }

   public final void reportError(String message, SourcePosition pos) {
      System.err.println("Error on line " + pos.getStart() + ": " + message);
      numErrors++;
   }

   public final void reportWarning(String message, SourcePosition pos) {
      System.err.println("Warning on line " + pos.getStart() + ": " + message);
      numWarnings++;
   }

}

