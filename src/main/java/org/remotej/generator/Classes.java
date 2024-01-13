package org.remotej.generator;

import javassist.CtClass;

/**
 * Copyright(c) Paul Soule 2006.
 * <p/>
 * Date: Feb 17, 2007
 * Time: 8:11:51 PM
 */
public class Classes {
   private String className;
   private CtClass generatedInterface;

   public String getClassName() {
      return className;
   }

   public void setClassName(String className) {
      this.className = className;
   }

   public CtClass getGeneratedInterface() {
      return generatedInterface;
   }

   public void setGeneratedInterface(CtClass generatedInterface) {
      this.generatedInterface = generatedInterface;
   }
}
