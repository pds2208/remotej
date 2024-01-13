package org.remotej.generator;

import java.io.Serializable;
import java.util.Random;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.remotej.ddl.trees.Parameter;

/**
 * Copyright(c) Paul Soule 2006.
 * <p/>
 * Date: Feb 17, 2007
 * Time: 6:13:42 AM
 */
public class Transfer implements Serializable {
   private static final long serialVersionUID = 2316164150335742539L;
   private String className;
   private String method;
   private Object[] parameters;
   @SuppressWarnings("unchecked")
   private Class[]  parameterTypes;
  private Object returnValue; // the value returned to the client
   private Parameter.CALL_TYPE returnType; // type type of parameter
   private String currentHost;
   private String identifier;

   public Transfer() {
      Random generator = new Random();
      InetAddress addr;
      try {
         addr = InetAddress.getLocalHost();
      } catch (UnknownHostException e) {
         identifier = "unknown" + generator;
         return;
      }
      identifier = addr.getHostAddress() + generator;
   }

//   public String getReturnObject() {
//      return returnObject;
//   }

//   public void setReturnObject(String returnObject) {
//      this.returnObject = returnObject;
//   }

   public Parameter.CALL_TYPE getReturnType() {
      return returnType;
   }

   public void setReturnType(String returnType) {
      if (returnType.equals("COPY")) {
         this.returnType = Parameter.CALL_TYPE.COPY;
      } else if (returnType.equals("EFERENCE")) {
         this.returnType = Parameter.CALL_TYPE.REFERENCE;
      } else if (returnType.equals("RESTORE")) {
         this.returnType = Parameter.CALL_TYPE.RESTORE; 
      } else {
         this.returnType = Parameter.CALL_TYPE.COPY;
      }
   }

   public String getIdentifier() {
      return identifier;
   }

   public String getCurrentHost() {
      return currentHost;
   }

   public void setCurrentHost(String currentHost) {
      this.currentHost = currentHost;
   }

   @SuppressWarnings("unchecked")
   public Class[] getParameterTypes() {
      return parameterTypes;
   }

   @SuppressWarnings("unchecked")
   public void setParameterTypes(Class[] parameterTypes) {
      this.parameterTypes = parameterTypes;
   }

   public String getClassName() {
      return className;
   }

   public void setClassName(String className) {
      this.className = className;
   }

   public String getMethod() {
      return method;
   }

   public void setMethod(String method) {
      this.method = method;
   }

   public Object[] getParameters() {
      return parameters;
   }

   public void setParameters(Object[] parameters) {
      this.parameters = parameters;
   }

   public Object getReturnValue() {
      return returnValue;
   }

   public void setReturnValue(Object returnValue) {
      this.returnValue = returnValue;
   }
}
