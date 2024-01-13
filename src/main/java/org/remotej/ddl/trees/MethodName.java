package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Created by IntelliJ IDEA.
 * User: Paul Soule
 * Date: 10-Apr-2006
 * Time: 16:16:16
 * To change this template use File | Settings | File Templates.
 */
public final class MethodName extends Statement {
   public final Identifier methodValue;

   public MethodName(Identifier methodValue, SourcePosition thePosition) {
      super(thePosition);
      this.methodValue = methodValue;
   }

   public final Object visit(Visitor v, Object o) {
      return v.visitMethodName(this, o);
   }
}
