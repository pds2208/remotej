package org.remotej.generator;

import java.util.Vector;

public class ServiceDescription {
    private String name;
    private Vector<JavaMethod> recovery = new Vector<JavaMethod>();
    private Vector<JavaMethod> advice = new Vector<JavaMethod>();
    private Vector<ProtocolDescription> export = new Vector<ProtocolDescription>();

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final Vector<JavaMethod> getRecovery() {
        return recovery;
    }

    public final void setRecovery(Vector<JavaMethod> recovery) {
        this.recovery = recovery;
    }

    public final Vector<JavaMethod> getAdvice() {
        return advice;
    }

    public final void setAdvice(Vector<JavaMethod> advice) {
        this.advice = advice;
    }

    public final Vector<ProtocolDescription> getProtocols() {
        return export;
    }

    public final void setProtocols(Vector<ProtocolDescription> export) {
        this.export = export;
    }
}
