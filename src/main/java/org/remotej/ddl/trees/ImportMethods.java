package org.remotej.ddl.trees;

import org.remotej.ddl.analyser.SourcePosition;

/**
 * Created by IntelliJ IDEA.
 * User: Paul Soule
 * Date: 12-Apr-2006
 * Time: 11:26:08
 * To change this template use File | Settings | File Templates.
 */
public final class ImportMethods extends ImportStatement {
   public final Methods methods;

   public ImportMethods(Methods methods, SourcePosition thePosition) {
      super(thePosition);
      this.methods = methods;
   }

   public final Object visit(Visitor v, Object o) {
      return v.visitImportMethods(this, o);
   }
}