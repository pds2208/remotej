package org.remotej.ddl.analyser;

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
 * File Created at 09:20:57 on 16-Jan-2006
 */
public final class SourcePosition {

   private int start;
   private int end;

   public SourcePosition() {
      start = 0;
      end = 0;
   }

   public final int getEnd() {
      return end;
   }

   public final void setEnd(int end) {
      this.end = end;
   }

   public final int getStart() {
      return start;
   }

   public final void setStart(int start) {
      this.start = start;
   }

   public SourcePosition(int start, int end) {
      this.start = start;
      this.end = end;
   }

   public final String toString() {
      return "(" + start + ", " + end + ")";
   }
}

