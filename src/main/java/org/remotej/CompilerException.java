package org.remotej;

import java.io.PrintStream;
import java.io.PrintWriter;

public final class CompilerException extends RuntimeException {

    /**
     * Exception that might have caused this one.
     */
    private Throwable cause;

    /**
     * Constructs a build exception with no descriptive information.
     */
    public CompilerException() {
        super();
    }

    /**
     * Constructs an exception with the given descriptive message.
     *
     * @param message A description of or information about the exception.
     *                Should not be <code>null</code>.
     */
    public CompilerException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the given message and exception as
     * a root cause.
     *
     * @param message A description of or information about the exception.
     *                Should not be <code>null</code> unless a cause is specified.
     * @param cause   The exception that might have caused this one.
     *                May be <code>null</code>.
     */
    public CompilerException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }


    /**
     * Constructs an exception with the given exception as a root cause.
     *
     * @param cause The exception that might have caused this one.
     *              Should not be <code>null</code>.
     */
    public CompilerException(Throwable cause) {
        super(cause.toString());
        this.cause = cause;
    }

    /**
     * Returns the nested exception, if any.
     *
     * @return the nested exception, or <code>null</code> if no
     * exception is associated with this one
     */
    public final Throwable getException() {
        return cause;
    }

    /**
     * Returns the nested exception, if any.
     *
     * @return the nested exception, or <code>null</code> if no
     * exception is associated with this one
     */
    public final Throwable getCause() {
        return getException();
    }

    /**
     * Prints the stack trace for this exception and any
     * nested exception to <code>System.err</code>.
     */
    public final void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Prints the stack trace of this exception and any nested
     * exception to the specified PrintStream.
     *
     * @param ps The PrintStream to print the stack trace to.
     *           Must not be <code>null</code>.
     */
    public final void printStackTrace(PrintStream ps) {
        synchronized (ps) {
            super.printStackTrace(ps);
            if (cause != null) {
                ps.println("--- Nested Exception ---");
                cause.printStackTrace(ps);
            }
        }
    }

    /**
     * Prints the stack trace of this exception and any nested
     * exception to the specified PrintWriter.
     *
     * @param pw The PrintWriter to print the stack trace to.
     *           Must not be <code>null</code>.
     */
    public final void printStackTrace(PrintWriter pw) {
        synchronized (pw) {
            super.printStackTrace(pw);
            if (cause != null) {
                pw.println("--- Nested Exception ---");
                cause.printStackTrace(pw);
            }
        }
    }
}
