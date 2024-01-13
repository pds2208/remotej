package org.remotej.ddl.analyser;

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
