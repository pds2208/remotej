package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

import java.util.LinkedList;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 * <p/>
 * All rights reserved.
 * <p/>
 * File Created at 17:01:24 on 01-Feb-2006
 */

public final class ParameterDeclaration extends Statement {

   /**
    * There are two types of parameters; one where there is a type and a value and the other
    * where there is just a value.
    */

   public final LinkedList<Parameter> parameters = new LinkedList<Parameter>();

   public ParameterDeclaration(SourcePosition thePosition) {
      super(thePosition);
   }

   public final void add(Parameter.CALL_TYPE callType, Identifier type, Identifier value) {
      parameters.add(new Parameter(callType, type, value));
   }

   public final Object visit(Visitor v, Object o) {
      return v.visitParameterDeclaration(this, o);
   }

}
