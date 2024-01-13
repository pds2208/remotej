package org.remotej.ddl.analyser;

import java.io.IOException;

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
        } catch (java.io.IOException s) {
            source = null;
            currentLine = 0;
        }
    }

    final char peek() {
        return (char) ahead;
    }

    /**
     * Implement a one character look ahead
     *
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
        } catch (java.io.IOException s) {
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
