/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

public class NameValueOption extends Option {
    public Identifier name;
    public Identifier value;

    public NameValueOption(Identifier name, Identifier value, SourcePosition thePosition) {
        super(thePosition);
        this.name = name;
        this.value = value;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitNameValueOption(this, o);
    }

}
