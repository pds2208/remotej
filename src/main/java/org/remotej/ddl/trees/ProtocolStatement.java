package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public class ProtocolStatement extends Statement {

    public String protocol;

    public ProtocolStatement(String protocol, SourcePosition thePosition) {
        super(thePosition);
        this.protocol = protocol;
    }

    public final Object visit(Visitor v, Object o) {
        return v.visitProtocolStatement(this, o);
    }

}
