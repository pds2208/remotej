package org.remotej.ddl;


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
