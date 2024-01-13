package org.remotej.generator;

import javassist.*;

import org.remotej.Compiler;
import org.remotej.ddl.trees.Parameter;

import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Copyright(c) Paul Soule. All rights reserved. <p/> This file is part of the
 * RemoteJ system. <p/> Created at 3:28:26 PM on Mar 26, 2006
 */
public final class NEWRMIProtocol extends Protocol {

   private static String REGISTRY_NAME = "registryName";
   private static String REGISTRY_HOST = "registryHost";
   private static String REGISTRY_PORT = "registryPort";
   private static String RUN_EMBEDDED_REGISTRY = "runEmbeddedRegistry";
   private static String HOSTS = "servers";

   private String registryName;
   private String registryHost;
   private int registryPort;
   private boolean runEmbeddedRegistry;
   private String hosts;

   private Vector<String> referenceObjects = new Vector<String>();

   public NEWRMIProtocol() {
   }

   /**
    * Generate the <code>Main</code> routine and the RMI server class file.
    */
   public final void generateMain() {
      Compiler.debug("Generating main file...");
      CtClass main;

      addPackages(pool);

      String cls = "org.remotej.RMIServer";
      // Add the client path to the server so it can find the generated
      // interfaces
      try {
         pool.appendClassPath("client");
      } catch (NotFoundException e) {
         e.printStackTrace();
      }

      main = pool.makeClass(cls);

      CtClass generated = null;
      // generate field(s) to hold our generated class
      int i = 0;
      HashMap<String, Vector<MethodDescription>> map = protocolDescription
            .getMethodsByClass();
      String calls = "System.setSecurityManager(new RMISecurityManager());\n";

      if (runEmbeddedRegistry) {
         calls += "java.rmi.registry.Registry registry = java.rmi.registry.LocateRegistry.createRegistry("
               + registryPort + ");\n";
      } else {
         calls += "java.rmi.registry.Registry registry = java.rmi.registry.LocateRegistry.getRegistry("
               + "\"" + registryHost + "\"" + ", " + registryPort + ");\n";
      }

      if (serverPlugin != null) {
         calls += "new " + serverPlugin + "().start();\n";
      }

      for (String className : map.keySet()) {
         String inf = "org.remotej.common._I" + getBaseClassName(className);
         try {
            generated = findClass(pool, className);
            CtField f = new CtField(generated, "rem" + i, main);
            f.setModifiers(Modifier.STATIC);
            main.addField(f);
            calls += " rem" + i + " = new " + generated.getName() + "();\n"
                  + inf + " stub" + i + " = (" + inf
                  + ") java.rmi.server.UnicastRemoteObject.exportObject(rem"
                  + i + ", 0); \n";

            calls += "registry.bind(\"" + registryName + "_" + className
                  + "\", stub" + i + ");\n";

            i++;
         } catch (CannotCompileException e) {
            reporter.reportError("cannot create field for "
                  + generated.getName(), lineNumber);
         }
      }

      i = 0;

      CtMethod meth;
      // now generate a main method
      try {
         // String version = Compiler.getRemoteJVersion().replaceAll("\n",
         // "");
         calls = "public static void main(String args[]) {\n" + "  try {\n"
               + calls;
         calls += "System.err.println();\n";
         // calls += "System.err.println(\"" + version + "\");\n";
         calls += "System.err.println(\"RMI Server ready\");\n "
               + "} catch (Exception e) { \n"
               + "System.err.println(\"RMI Server exception: \" + e.toString()); \n"
               + "e.printStackTrace();\n" + "}\n" + "}\n";
         Compiler.debug(calls);
         // System.err.println(calls);
         meth = CtNewMethod.make(calls, main);
         CtMethod method;
         try {
            method = getMethodByName(main, "main");
            main.removeMethod(method);
         } catch (NotFoundException e) {
         }
         main.addMethod(meth);
      } catch (CannotCompileException e) {
         reporter.reportError("cannot create main method: " + e, lineNumber);
      }
      // now write the file
      try {
         main.writeFile(serverOutputDirectory
               + System.getProperty("file.separator") + super.getService());
      } catch (IOException e) {
         reporter.reportError("I/O error when compiling " + cls, lineNumber);
      } catch (CannotCompileException e) {
         reporter.reportError("compile failed for " + cls, lineNumber);
      }
   }

   /**
    * Start the generation
    */
   public void generate() {
      Compiler.debug("RMI Generation...");
      Compiler.debug("Class Name       : "
            + protocolDescription.getCurrentMethod().getClassName());
      Compiler.debug("Registry Name    : " + registryName);
      Compiler.debug("Registry Port    : " + registryPort);
      Compiler.debug("Embedded Registry: " + runEmbeddedRegistry);

      // CtClass generatedInterface = generateInterface();
      Vector<Classes> generatedInterfaces;

      generatedInterfaces = generateClient();
      if (generatedInterfaces != null) {
         generateServer(generatedInterfaces);
         generateMain();
      }

   }

   /**
    * Generate an interface for the exported class
    * 
    * @param cls
    *           the class to have an interface generated from
    * @param md
    *           a <code>Vector</code> of methods to add to the interface
    * @return the CtClass representing the interface
    */
   public CtClass generateInterface(CtClass cls, Vector<MethodDescription> md) {
      String inf = "org.remotej.common._I" + cls.getSimpleName();

      addPackages(pool);

      CtClass generatedInterface = pool.makeInterface(inf);
      generatedInterface.stopPruning(true);

      try {
         // implement java.rmi.Remote
         CtClass remote = pool.get("java.rmi.Remote");
         generatedInterface.addInterface(remote);
      } catch (NotFoundException e) {
         e.printStackTrace();
      }

      CtClass c = findClass(pool, cls.getName());
      if (c == null) {
         reporter.reportError("class: "
               + protocolDescription.getCurrentMethod().getClassName()
               + ", does not exist.", lineNumber);
         return generatedInterface;
      }

      // all selected methods

      for (MethodDescription meth : md) {
         CtMethod method;
         try {
            method = getMethod(c, meth);
         } catch (NotFoundException e) {
            reporter.reportError("method: " + meth.getName()
                  + ", does not exist.", lineNumber);
            return null;
         }

         String returnType = checkReturnType(meth, method, method.getName());
         // Check the parameters are serializable
         LinkedList<Parameter> parameters = meth.getParameters().parameters;
         for (int i = 0; i < parameters.size(); i++) {
            Parameter p = parameters.get(i);
            p.value.spelling = "a" + i;
            CtClass pc = super.findClass(pool, p.type.spelling);
            assert pc != null;
            if (pc.isPrimitive())
               continue;
            p.type.spelling = pc.getName();
            if (p.callType == Parameter.CALL_TYPE.REFERENCE) {
               String is = makeRemote(p.type.spelling);
               if (is == null)
                  return null;
               p.referenceType = is;
               System.err.println("changing type to: " + is);
            }
         }
         // LinkedList parameters;
         // try {
         // parameters = createParameters(method);
         // } catch (NotFoundException e) {
         // reporter.reportError("error reading parameters for method: "
         // + method.getName(), lineNumber);
         // return generatedInterface;
         // }
         // Build up parameter string
         String parameterList = makeParameterList(parameters);

         // add to the interface

         try {
            String s = "public " + returnType + " " + method.getName() + "("
                  + parameterList + ") throws java.rmi.RemoteException;";
            Compiler.debug("Generating Interface method: " + s);
            CtMethod meth1 = CtNewMethod.make(s, generatedInterface);
            generatedInterface.addMethod(meth1);
         } catch (CannotCompileException e) {
            reporter.reportError("cannot create interface method: "
                  + method.getName(), lineNumber);
            return generatedInterface;
         }
      }

      try {
         generatedInterface.writeFile(serverOutputDirectory
               + System.getProperty("file.separator") + super.getService());
         generatedInterface.defrost();
      } catch (CannotCompileException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return generatedInterface;
   }

   private void setupClient(CtClass c) throws CannotCompileException {
      CtField f;
      CtMethod m;

      f = CtField
            .make(
                  "private static java.rmi.registry.Registry currentRegistry = null;",
                  c);
      c.addField(f);
      f = CtField.make("private String[] hosts = null;", c);
      c.addField(f);
      f = CtField.make("private int exceptionDepth = 0;", c);
      c.addField(f);
      f = CtField.make("private static int currentHost = 0;", c);
      c.addField(f);
      f = CtField.make("private boolean hostsChanged = false;", c);
      c.addField(f);
      f = CtField
            .make(
                  "private static org.remotej.generator.Transfer transfer = new org.remotej.generator.Transfer();",
                  c);
      c.addField(f);

      m = CtMethod
            .make(
                  "public void _RMIClient() { "
                        + "  String s;"
                        + "  if (System.getProperty(\"remotej.servers\") != null) { "
                        + "    s = System.getProperty(\"remotej.servers\"); "
                        + "  } else { "
                        + "    s = \""
                        + hosts
                        + "\";"
                        + "  } "
                        + "  java.util.StringTokenizer st = new java.util.StringTokenizer(s, \",\");"
                        + "  hosts = new String[st.countTokens()];"
                        + "  int i = 0;" + "  while (st.hasMoreTokens()) {"
                        + "    hosts[i++] = st.nextToken();" + "  }" + "}", c);
      c.addMethod(m);

      m = CtMethod.make("public void setHosts(String[] hosts) { "
            + "String s = \"\";" + "for (int i = 0; i < hosts.length; i++) { "
            + "   s += hosts[i];" + "   if (i != hosts.length - 1) {"
            + "      s+= \",\";" + "   }" + "}"
            + "System.setProperty(\"remotej.servers\", s);"
            + "hostsChanged = true;" + "}", c);
      c.addMethod(m);

      m = CtMethod.make("public String getCurrentHost() { "
            + "  if (hosts == null) { " + "    _RMIClient(); " + "  }"
            + "  return hosts[currentHost];" + "}", c);
      c.addMethod(m);

      m = CtMethod
            .make(
                  "public void findAlternateServer() throws java.rmi.RemoteException {"
                        + "  _RMIClient(); "
                        + "  if (hosts == null) { "
                        + "     throw new java.rmi.RemoteException(\"No RMI server hosts have been declared on the command line\");"
                        + "  }"
                        + "  int savedHost = currentHost;"
                        + "  while (true) {"
                        + "     System.err.println(\"Connecting to: \" + hosts[currentHost]);"
                        + "     try { "
                        + "       java.rmi.registry.Registry registry =  java.rmi.registry.LocateRegistry.getRegistry(hosts[currentHost],"
                        + registryPort
                        + ");"
                        + "       registry.list(); "
                        + // to force a connection
                        "       currentRegistry = registry;"
                        + "       return; "
                        + "     } catch (java.rmi.RemoteException e) { "
                        + "       System.err.println(\"Connection to host: \" + hosts[currentHost] + \" failed.\");"
                        + "     }"
                        + "     currentHost++;"
                        + "     if (currentHost + 1 > hosts.length) {"
                        + "        currentHost = 0;"
                        + "     } "
                        + "     if (currentHost == savedHost) { "
                        + "        exceptionDepth++; "
                        + "        throw new java.rmi.RemoteException(\"Cannot connect to any remote server(s)\");"
                        + "     }" + "  }" + "}", c);
      c.addMethod(m);

      m = CtMethod.make(
            "public synchronized java.rmi.registry.Registry getRegistry() {"
                  + "   if (currentRegistry == null) {"
                  + "      findAlternateServer(); " + "   }"
                  + "   return currentRegistry;" + "}", c);
      c.addMethod(m);

      m = CtMethod.make("public int getExceptionDepth() { \n"
            + "  return exceptionDepth;\n" + "}\n", c);
      c.addMethod(m);
   }

   public Vector<Classes> generateClient() {
      Vector<Classes> v = new Vector<Classes>();
      addPackages(pool);

      CtClass c;

      // for each matching class / methods
      // generate the code

      HashMap<String, Vector<MethodDescription>> map = protocolDescription
            .getMethodsByClass();

      for (String className : map.keySet()) {
         c = findClass(pool, className);
         if (c == null) {
            reporter.reportError("class: " + className + ", does not exist.",
                  lineNumber);
            return null;
         }

         if (!super.hasNullConstructor(c)) {
            reporter.reportError("class: " + c.getName()
                  + ", does not have a null constructor.", lineNumber);
            return null;
         }

         try {
            setupClient(c);
         } catch (CannotCompileException e) {
            reporter.reportError("cannot add RMI methods to class: "
                  + className, lineNumber);
            return null;
         }

         CtClass inf = generateInterface(c, map.get(className));
         Classes clsses = new Classes();
         clsses.setClassName(className);
         clsses.setGeneratedInterface(inf);
         v.add(clsses);

         for (MethodDescription md : map.get(className)) {
            CtMethod method;
            try {
               method = getMethod(c, md);
            } catch (NotFoundException e) {
               reporter.reportError("method: " + md.getName()
                     + ", does not exist.", lineNumber);
               System.err.println(e);
               return null;
            }

            super.checkSynchronized(method);

            String returnType = checkReturnType(md, method, method.getName());
            if (returnType == null) {
               return null;
            }

            // if (md.getReturnValue().type == Parameter.CALL_TYPE.REFERENCE) {
            // String i = makeRemote(returnType);
            // if (i == null)
            // return null;
            // // returnType = i;
            // }

            // Check the parameters are serializable
            LinkedList<Parameter> parameters = md.getParameters().parameters;
//             for (int i = 0; i < parameters.size(); i++) {
//               Parameter p = parameters.get(i);
//               CtClass pc = super.findClass(pool, p.type.spelling);
//               assert pc != null;
//               if (pc.isPrimitive())
//                  continue;
//               p.type.spelling = pc.getName();
//               if (p.callType == Parameter.CALL_TYPE.REFERENCE) {
//                  String is = makeRemote(p.type.spelling);
//                  if (is == null)
//                     return null;
//                  p.generatedInterface = is;
//               }
//            }

            try {
               parameters = createParameters(method);
            } catch (NotFoundException e) {
               reporter.reportError("error reading parameters for method: "
                     + method.getName(), lineNumber);
               return null;
            }

            // Build up parameter string
            String parameterList = makeParameterList(parameters);
            String callingParameters = makeParameterValues(parameters);
            String recoveryName = getRecoveryName(method.getName());

            boolean abort = false;
            boolean nextServer = false;
            boolean continueApp = false;
            boolean recoveryRoutine = false;

            if (recoveryName == null || "".equals(recoveryName)) {
               recoveryName = "abort";
            }
            if (recoveryName.equals("nextServer")) {
               nextServer = true;
            } else if (recoveryName.equals("continue")) {
               continueApp = true;
            } else if (recoveryName.equals("abort")) {
               abort = true;
            } else {
               recoveryRoutine = true;
            }
            // generate the call
            try {
               String methodBody = "";
               if (recoveryRoutine) {
                  if (!generateRecoveryRoutine(c, recoveryName, returnType)) {
                     return null;
                  }
               }
               methodBody = "public " + returnType + " " + method.getName()
                     + "(" + parameterList
                     + ") throws java.rmi.RemoteException {\n";

               methodBody += super.setupTransferObject(method, c, parameters);

               String lookup = "     org.remotej.common." + inf.getSimpleName()
                     + " stub = (" + "org.remotej.common."
                     + inf.getSimpleName() + ") getRegistry().lookup(" + "\""
                     + registryName + "_" + md.getClassName() + "\");\n";

               String methodCall = "stub." + method.getName() + "("
                     + callingParameters + ");\n";

               // generate call like so
               // boolean done = false;
               // do {
               // try {
               // return callRemoteMethod();
               // } catch (Exception e) {
               // if (abort) {
               // e.printStackTrace();
               // System.exit(1);
               // } else if (nextServer) {
               // findAlternateServer();
               // } else if (continueApp) {
               // done = true;
               // } else if (recoveryRoutine) {
               // recoveryRoutine(e);
               // }
               // }
               // } while (!done);
               // return null;

               // if (nextServer || continueApp || abort) {
               methodBody += "  boolean done = false;\n";
               methodBody += "  int savedHost = currentHost;";
               methodBody += "  do {\n" + "   try {\n" + lookup;
               if (md.getReturnValue() != null
                     && !"void".equals(md.getReturnValue())) {
                  methodBody += "      return " + methodCall;
               } else {
                  methodBody += methodCall + "return;\n";
               }

               if (abort) {
                  methodBody += "   } catch (java.rmi.RemoteException e) {\n"
                        + "     System.err.println(\"Aborting...\");\n"
                        + "     e.printStackTrace();\n"
                        + "     System.exit(1);\n" + "   }\n"
                        + "} while (!done);\n";
               } else if (nextServer) {
                  methodBody += "   } catch (java.rmi.RemoteException e) {\n"
                        + "     findAlternateServer();\n" + "   }\n"
                        + "} while (!done);\n";
               } else if (continueApp) {
                  methodBody += "   } catch (java.rmi.RemoteException e) {\n"
                        + "     done = true;" + "   }\n" + "} while (!done);\n";
               } else if (recoveryRoutine) {
                  JavaMethod recoveryMethod = getRecovery(recoveryName);
                  methodBody += "   } catch ("
                        + recoveryMethod.getParameters().parameters.getFirst().type.spelling
                        + " e) {\n"
                        +
                        // " _recoveryRoutine(" +
                        recoveryMethod.getName()
                        + "("
                        + recoveryMethod.getParameters().parameters.getFirst().value.spelling
                        + ");\n" + "   }\n" + "   if (hostsChanged) {\n"
                        + "     hostsChanged = false; \n"
                        + "     exceptionDepth++; \n"
                        + "     findAlternateServer();\n " + "   }\n"
                        + "  } while (!done);\n";
               }

               if ("void".equals(returnType)) {
                  methodBody += "  return;\n}\n";
               } else if ("byte".equals(returnType)
                     || "short".equals(returnType) || "int".equals(returnType)
                     || "long".equals(returnType) || "char".equals(returnType)) {
                  methodBody += "  return 0;\n}\n";
               } else if ("float".equals(returnType)
                     || "double".equals(returnType)) {
                  methodBody += "  return 0.0;\n}\n";
               } else if ("boolean".equals(returnType)) {
                  methodBody += "  return false;\n}\n";
               } else {
                  methodBody += "  return null;\n}\n";
               }

               // } else if (recoveryRoutine) {
               // methodBody += lookup;
               // if (!"void".equals(returnType)) {
               // methodBody += " return ";
               // }
               //
               // methodBody += " stub." + method.getName() + "(" +
               // callingParameters + ");\n" + "}\n";
               // }

               Compiler.debug(methodBody);
               CtMethod mmm = CtMethod.make(methodBody, c);

               // Add the recovery routine, if it exists
               // if (recoveryRoutine) {
               // generateRecoveryRoutine(recoveryName, returnType, mmm);
               // }

               c.removeMethod(method);
               c.addMethod(mmm);

            } catch (CannotCompileException e) {
               reporter.reportError("cannot create client method: "
                     + method.getName(), lineNumber);
            } catch (NotFoundException e) {
               reporter.reportError("cannot delete client method: "
                     + method.getName(), lineNumber);
            }
         }

         try {
            c.writeFile(clientOutputDirectory
                  + System.getProperty("file.separator") + super.getService());
            c.detach();
            inf.detach();
            c = null;
            inf = null;
         } catch (CannotCompileException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

      return v;
   }

   private String makeRemote(String returnType) {
      // may be an array so get the real type
      CtClass c = super.findClass(pool, returnType);
      try {
         if (c.getComponentType() != null) {
            c = c.getComponentType();
         }
      } catch (NotFoundException e) {
         return null;
      }

      String inf = "org.remotej.common._I" + c.getSimpleName();
      String arry;
      if (returnType.contains("[")) {
         arry = returnType.substring(returnType.indexOf('['));
      } else {
         arry = c.getSimpleName();
      }

      if (referenceObjects.contains(c.getName())) {
         System.err.println("Already made " + c.getName() + " remote");
         return inf;
      }

      System.err.println("Making remote: " + c.getName());
      c.stopPruning(true);

      addPackages(pool);

      CtClass generatedInterface = pool.makeInterface(inf);
      generatedInterface.stopPruning(true);

      try {
         // implement java.rmi.Remote
         CtClass remote = pool.get("java.rmi.Remote");
         generatedInterface.addInterface(remote);
      } catch (NotFoundException e) {
         e.printStackTrace();
      }

      // a exported filed
      CtField f;
      try {
         f = CtField.make("private boolean _exported = false;", c);
         c.addField(f);
      } catch (CannotCompileException e1) {
         reporter.reportError("cannot add _exported field ", lineNumber);
         e1.printStackTrace();
      }

      CtMethod[] methods = c.getDeclaredMethods();

      for (int i = 0; i < methods.length; i++) {
         if (!Modifier.isPublic(methods[i].getModifiers())) {
            continue;
         }
         try {
            CtClass[] exceptions = methods[i].getExceptionTypes();
            CtClass[] ec1 = new CtClass[exceptions.length + 1];
            for (int j = 0; j < exceptions.length; j++) {
               ec1[j] = exceptions[j];
            }
            CtClass re = pool.get("java.rmi.RemoteException");
            assert (re != null);
            ec1[ec1.length - 1] = re;
            CtMethod meth1 = CtNewMethod.abstractMethod(methods[i]
                  .getReturnType(), methods[i].getName(), methods[i]
                  .getParameterTypes(), ec1, generatedInterface);
            generatedInterface.addMethod(meth1);
            methods[i].setExceptionTypes(ec1);
         } catch (CannotCompileException e) {
            reporter.reportError("cannot create interface method: "
                  + methods[i].getName(), lineNumber);
            System.err.println(e);
            return null;
         } catch (NotFoundException e) {
            e.printStackTrace();
            reporter.reportError("cannot create interface method: "
                  + methods[i].getName(), lineNumber);
            return null;
         }

      }

      referenceObjects.add(c.getName());
      c.addInterface(generatedInterface);

      methods = c.getDeclaredMethods();

      String methodBody = "  if (_exported == false) {\n"
            + "     _exported = true;\n"
            + "     java.rmi.server.UnicastRemoteObject.exportObject(this, 0);\n"
            + "  }";

      for (int i = 0; i < methods.length; i++) {
         if (!Modifier.isPublic(methods[i].getModifiers())) {
            continue;
         }
         try {
            methods[i].insertBefore(methodBody);
         } catch (CannotCompileException e) {
            e.printStackTrace();
            reporter.reportError("cannot insert exportObject in method: "
                  + methods[i].getName(), lineNumber);
            return null;
         }
      }
      try {
         generatedInterface.writeFile(serverOutputDirectory
               + System.getProperty("file.separator") + super.getService());
         c.writeFile(serverOutputDirectory
               + System.getProperty("file.separator") + super.getService());
         c.defrost();
         c.writeFile(clientOutputDirectory
               + System.getProperty("file.separator") + super.getService());
      } catch (CannotCompileException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      return inf;
   }

   public void generateServer(Vector<Classes> generatedInterface) {

      addPackages(pool);
      HashMap<String, Vector<MethodDescription>> map = protocolDescription
            .getMethodsByClass();

      for (Classes classes : generatedInterface) {
         CtClass c = findClass(pool, classes.getClassName());
         c.stopPruning(true);
         alterToImplementInterface(c, classes.getGeneratedInterface());
         for (MethodDescription md : map.get(classes.getClassName())) {
            CtMethod method;
            try {
               method = getMethod(c, md);
            } catch (NotFoundException e) {
               reporter.reportError("method: " + md.getName()
                     + ", does not exist.", lineNumber);
               return;
            }
            makeExportMethod(md, c, method);
         }

      }

   }

   /**
    * Make a subclass that implements <code>java.rmi.Remote</code>
    * 
    * @param ctClass
    *           the class name
    * @param method
    *           the method to alter
    */

   public final void makeExportMethod(MethodDescription md, CtClass ctClass,
         CtMethod method) {

      String methodName = method.getName();

      if (!Modifier.isPublic(method.getModifiers())) {
         reporter.reportError("method: " + methodName + " in class: "
               + ctClass.getName() + ", is not public", lineNumber);
      }
      // if (Modifier.isFinal(m.getModifiers())) {
      // reporter.reportError("method: " + methodName + " in class: " +
      // ctClass.getName() + ", is final", lineNumber);
      // }
      if (Modifier.isNative(method.getModifiers())) {
         reporter.reportError("method: " + methodName + " in class: "
               + ctClass.getName() + ", is native", lineNumber);
      }
      // if (Modifier.isStatic(m.getModifiers())) {
      // reporter.reportError("method: " + methodName + " in class: " +
      // ctClass.getName() + ", is static", lineNumber);
      // }
      if (Modifier.isTransient(method.getModifiers())) {
         reporter.reportError("method: " + methodName + " in class: "
               + ctClass.getName() + ", is transient", lineNumber);
      }

      checkReturnType(md, method, methodName); // the type of the return, e.g.
      // void, int etc.

      // check parameter types
      LinkedList<Parameter> parameters = md.getParameters().parameters;
      boolean altered = false;
      for (int i = 0; i < parameters.size(); i++) {
         Parameter p = parameters.get(i);
         CtClass pc = super.findClass(pool, p.type.spelling);
         assert pc != null;
         if (pc.isPrimitive())
            continue;
         p.type.spelling = pc.getName();
         if (p.callType == Parameter.CALL_TYPE.REFERENCE) {
            String is = makeRemote(p.type.spelling);
            if (is == null)
               return;
            p.generatedInterface = is;
            altered = true;
         }
      }

      if (altered) {
         String pString = "";
         String aString = "";
         for (int i = 0; i < parameters.size(); i++) {
            Parameter p = parameters.get(i);
            if (p.generatedInterface != null) {
               pString += p.generatedInterface + " a" + i;
               //aString += "(" + p.type.spelling + ") a" +i;
               aString += " a" +i;
            } else {
               pString += p.type.spelling + " a" + i;
               aString += " a" +i;
            }
            
            if (i != parameters.size() - 1) {
               pString += ", ";
               aString += ", ";
            }
         }
         String s;
         try {
            s = "public " + method.getReturnType().getName() + " " +
               method.getName() +  " (" + pString + ") {\n";
            s += "   _" + method.getName() + "(" + aString + ");\n";
            s += "}\n";
            Compiler.debug(s);
            
            CtMethod m = CtNewMethod.copy(method, "_" + method.getName(),
                  ctClass, null);
            ctClass.addMethod(m); // add a duplicate method
            m = CtNewMethod.make(s, ctClass);
            //m.setModifiers(m.getModifiers() & ~Modifier.ABSTRACT);
            ctClass.addMethod(m);
         } catch (NotFoundException e) {
            e.printStackTrace();
         } catch (CannotCompileException e) {
            e.printStackTrace();
         }
      }

      // LinkedList<Parameter> parameters;
      // try {
      // parameters = createParameters(method);
      // } catch (NotFoundException e) {
      // reporter.reportError("error reading parameters for method: "
      // + method.getName(), lineNumber);
      // return;
      // }

      // Check the parameters are serializable
      // checkParameters(parameters);

      try {
         ctClass.writeFile(serverOutputDirectory
               + System.getProperty("file.separator") + super.getService());
         ctClass.defrost();
      } catch (IOException e) {
         reporter.reportError("I/O error when compiling " + methodName
               + "  method", lineNumber);
      } catch (CannotCompileException e) {
         reporter.reportError("compile failed for " + methodName + "  method",
               lineNumber);
      }
   }

   public void validateOptions() throws OptionException {

      registryName = protocolOptions.getOptionValue(REGISTRY_NAME);
      registryHost = protocolOptions.getOptionValue(REGISTRY_HOST);
      hosts = protocolOptions.getOptionValue(HOSTS);

      super.validateOptions();

      try {
         registryPort =  Integer.parseInt(protocolOptions.getOptionValue(REGISTRY_PORT));
      } catch (NumberFormatException e) {
         throw new OptionException(REGISTRY_PORT
               + " option does not contain an integer value", protocolOptions
               .getLineNo(REGISTRY_PORT));
      }

      String b = protocolOptions.getOptionValue(RUN_EMBEDDED_REGISTRY);

      if (!"true".equals(b) && !"false".equals(b)) {
         throw new OptionException("true or false expected for "
               + RUN_EMBEDDED_REGISTRY + " option", protocolOptions
               .getLineNo(RUN_EMBEDDED_REGISTRY));
      }

      runEmbeddedRegistry =  Boolean.parseBoolean(protocolOptions
            .getOptionValue(RUN_EMBEDDED_REGISTRY));

      // check for required protocolOptions
      if (registryName == null) {
         throw new OptionException("registryName has not been set");
      }
      if (registryHost == null) {
         throw new OptionException("registryHost has not been set");
      }
      // set these protocolOptions to defaults if they aren't set
      if (registryPort == 0) {
         registryPort = 1099;
      }
   }
}
