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
 * File Created at 09:19:52 on 16-Jan-2006
 */

public final class Scanner {
   private final SourceFile sourceFile;
   private boolean debug;

   private char currentChar;
   private StringBuffer currentSpelling;
   private boolean currentlyScanningToken;

   private boolean isLetter(char c) {
      return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
   }

   private boolean isDigit(char c) {
      return (c >= '0' && c <= '9');
   }

   public Scanner(SourceFile source) {
      sourceFile = source;
      currentChar = sourceFile.getSource();
      debug = false;
   }

   public final void enableDebugging() {
      debug = true;
   }

   // takeIt appends the current character to the current token, and gets
   // the next character from the source program.

   private void takeIt() {
      if (currentlyScanningToken) {
         currentSpelling.append(currentChar);
      }
      currentChar = sourceFile.getSource();
   }

   private void skipCharacter() {
      currentChar = sourceFile.getSource();
   }

   // scanSeparator skips a single separator.

   private void scanSeparator() {
      switch (currentChar) {
         case '/': {
            if (sourceFile.peek() == '/') {   // scan until EOL
               takeIt();
               while ((currentChar != SourceFile.EOL) && (currentChar != SourceFile.EOT)) {
                  takeIt();
               }
               if (currentChar == SourceFile.EOL) {
                  takeIt();
               }
            } else if (sourceFile.peek() == '*') { // scan until matching comment
               takeIt();
               while (true) {
                  if (currentChar == SourceFile.EOT) {
                     break;
                  }
                  if (currentChar == '*' && sourceFile.peek() == '/') {
                     takeIt();
                     takeIt();
                     break;
                  }
                  takeIt();
               }
            }
         }
         break;

         case ' ':
         case '\n':
         case '\r':
         case '\t':
            takeIt();
            break;
      }
   }

   public int peek() {
      return sourceFile.peek();
   }

   private int scanToken() {

      switch (currentChar) {

         case 'a':
         case 'b':
         case 'c':
         case 'd':
         case 'e':
         case 'f':
         case 'g':
         case 'h':
         case 'i':
         case 'j':
         case 'k':
         case 'l':
         case 'm':
         case 'n':
         case 'o':
         case 'p':
         case 'q':
         case 'r':
         case 's':
         case 't':
         case 'u':
         case 'v':
         case 'w':
         case 'x':
         case 'y':
         case 'z':
         case 'A':
         case 'B':
         case 'C':
         case 'D':
         case 'E':
         case 'F':
         case 'G':
         case 'H':
         case 'I':
         case 'J':
         case 'K':
         case 'L':
         case 'M':
         case 'N':
         case 'O':
         case 'P':
         case 'Q':
         case 'R':
         case 'S':
         case 'T':
         case 'U':
         case 'V':
         case 'W':
         case 'X':
         case 'Y':
         case 'Z':
         case '_':
         case '*':
            takeIt();
            while (isLetter(currentChar) || isDigit(currentChar) || currentChar == '*' || currentChar == '_')
               takeIt();
            return Token.IDENTIFIER;

         case '0':
         case '1':
         case '2':
         case '3':
         case '4':
         case '5':
         case '6':
         case '7':
         case '8':
         case '9':
            takeIt();
            while (isDigit(currentChar))
               takeIt();
            return Token.INTLITERAL;

         case '\'':
            takeIt();
            takeIt(); // the quoted character
            if (currentChar == '\'') {
               takeIt();
               return Token.CHARLITERAL;
            } else
               return Token.ERROR;

         case '.':
            takeIt();
            if (currentChar == '.') {
               takeIt();
               return Token.DOTDOT;
            }
            return Token.DOT;

         case ';':
            takeIt();
            return Token.SEMICOLON;

         case ',':
            takeIt();
            return Token.COMMA;

         case ':':
            takeIt();
            return Token.COLON;

         case '(':
            takeIt();
            return Token.LPAREN;

         case ')':
            takeIt();
            return Token.RPAREN;

         case '{':
            takeIt();
            return Token.LCURLY;

         case '}':
            takeIt();
            return Token.RCURLY;

         case '[':
            takeIt();
            return Token.LBRACKET;

         case ']':
            takeIt();
            return Token.RBRACKET;

         case '=':
            takeIt();
            return Token.EQUALS;

         case '"':
            skipCharacter();
            while ((currentChar != SourceFile.QUOTE) && (currentChar != SourceFile.EOL) && (currentChar != SourceFile.EOT))
            {
               takeIt();
            }
            if (currentChar == SourceFile.QUOTE) {
               skipCharacter();
            }
            return Token.IDENTIFIER;

         case SourceFile.EOT:
            return Token.EOT;

         default:
            takeIt();
            return Token.ERROR;
      }
   }

   /**
    * Scan any number of characters until we hit the final '}' character
    *
    * @return A string representing Java source code
    */
   public final String scanJavaCode(boolean topLevel) throws SyntaxError {
      StringBuffer code = new StringBuffer();
      while (true) {
         while ((currentChar != '{') && (currentChar != '}') && (currentChar != SourceFile.EOT)) {
            code.append(currentChar);
            currentChar = sourceFile.getSource();
         }
         if (currentChar == '{') {
            code.append(currentChar);
            currentChar = sourceFile.getSource();
            String s = scanJavaCode(false);
            code.append(s);
//            if (topLevel) {
               // we are back up to the top level so quit parsing
//               return code.toString();
//            }
         } else {
            if (currentChar == '}') {
               if (topLevel) {
                  return code.toString();
               }
               code.append(currentChar);
               currentChar = sourceFile.getSource();
               return code.toString();
            } else {
               code.append(currentChar);
               throw new SyntaxError("Unexpected end of file");
            }
         }
      }
   }

   public String scanURL() {

      StringBuffer url = new StringBuffer();
      while ((currentChar != ';') && (currentChar != SourceFile.EOT)) {
         url.append(currentChar);
         currentChar = sourceFile.getSource();
      }
      return url.toString();

   }
   public final Token scan() {
      Token tok;
      SourcePosition pos;
      int kind;

      currentlyScanningToken = false;

      while (currentChar == '/'
         || currentChar == ' '
         || currentChar == '\n'
         || currentChar == '\r'
         || currentChar == '\t')
         scanSeparator();


      currentlyScanningToken = true;
      currentSpelling = new StringBuffer("");
      pos = new SourcePosition();
      pos.setStart(sourceFile.getCurrentLine());

      kind = scanToken();

      pos.setEnd(sourceFile.getCurrentLine());
      tok = new Token(kind, currentSpelling.toString(), pos);
      if (debug) {
         System.out.println(tok);
      }
      return tok;
   }

}
