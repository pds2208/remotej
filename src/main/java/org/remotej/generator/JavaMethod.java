package org.remotej.generator;

import org.remotej.ddl.trees.ParameterDeclaration;

public final class JavaMethod {
   private String name;
   private ParameterDeclaration parameters;
   private String javaCode;

   public JavaMethod(String name, ParameterDeclaration parameters, String javaCode) {
      this.name = name;
      this.parameters = parameters;
      this.javaCode = javaCode;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public ParameterDeclaration getParameters() {
      return parameters;
   }

   public void setParameters(ParameterDeclaration parameters) {
      this.parameters = parameters;
   }

   public String getJavaCode() {
      return javaCode;
   }

   public void setJavaCode(String javaCode) {
      this.javaCode = javaCode;
   }
}
