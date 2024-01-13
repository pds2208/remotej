package org.remotej.ddl.analyser;

import java.io.IOException;

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
 * File Created at 10:02:48 on 16-Jan-2006
 */
public final class SourceFile {
   public static final char EOL = '\n';
   public static final char EOT = '\u0000';
   public static final char QUOTE = '\u0022';
   private static int ahead = 0;
   private static int current = 0;
   private java.io.FileInputStream source;
   private int currentLine;

   public SourceFile(String filename) {
      java.io.File sourceFile;
      try {
         sourceFile = new java.io.File(filename);
         source = new java.io.FileInputStream(sourceFile);
         currentLine = 1;
      }
      catch (java.io.IOException s) {
         source = null;
         currentLine = 0;
      }
   }

   final char peek() {
      return (char) ahead;
   }

   /**
    * Implement a one character look ahead
    * @return the next character in the file
    */
   final char getSource() {
      try {
         if (current == 0) { // first time
            current = readNext();
            if (current != EOT) {
               ahead = readNext();
            }
            return (char) current;
         }

         current = ahead;
         ahead = readNext();
         return (char) current;
      }
      catch (java.io.IOException s) {
         return EOT;
      }
   }

   final char readNext() throws IOException {
      int c = source.read();
      if (c == -1) {
         c = EOT;
      } else if (c == EOL) {
         currentLine++;
      }
      return (char) c;
   }

   final int getCurrentLine() {
      return currentLine;
   }
}

