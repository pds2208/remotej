package org.remotej.generator;

public class OptionException extends Exception {
    public int getLineNumber() {
        return lineNumber;
    }

    private int lineNumber;

    public OptionException(String message) {
        super(message);
    }

    public OptionException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }

}
