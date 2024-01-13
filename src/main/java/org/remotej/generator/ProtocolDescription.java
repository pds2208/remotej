package org.remotej.generator;

import java.util.HashMap;
import java.util.Vector;

/**
 * Copyright(c) Paul Soule. All rights reserved.
 * <p/>
 * This file is part of the RemoteJ system.
 * <p/>
 * Created at 3:15:57 PM on Jul 30, 2006
 */
public class ProtocolDescription {
   private String protocol;
   private Vector<MethodDescription> methods = new Vector<MethodDescription>();
   HashMap<String,Vector<MethodDescription>> methodsByClass;
   private ProtocolOptions protocolOptions = new ProtocolOptions();
   private MethodDescription currentMethod;

   public HashMap<String, Vector<MethodDescription>> getMethodsByClass() {
      return methodsByClass;
   }

   public void setMethodsByClass(HashMap<String, Vector<MethodDescription>> methodsByClass) {
      this.methodsByClass = methodsByClass;
   }

   public ProtocolOptions getProtocolOptions() {
      return protocolOptions;
   }

   public void setProtocolOptions(ProtocolOptions protocolOptions) {
      this.protocolOptions = protocolOptions;
   }

   public String getProtocol() {
      return protocol;
   }

   public void setProtocol(String protocol) {
      this.protocol = protocol;
   }

   public void setMethods(Vector<MethodDescription> methods) {
      this.methods = methods;
   }

   public MethodDescription getCurrentMethod() {
      return currentMethod;
   }

   public void setCurrentMethod(MethodDescription currentMethod) {
      this.currentMethod = currentMethod;
   }

   public void addMethod(MethodDescription method) {
      currentMethod = method;
      methods.add(method);
   }

   public MethodDescription getcurrentMethod() {
      return currentMethod;
   }

   public Vector<MethodDescription> getMethods() {
      return methods;
   }

}
