package org.remotej.generator;

import org.remotej.Compiler;
import org.remotej.ddl.trees.Parameter;

import javassist.*;

import java.io.IOException;
import java.util.Vector;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * User: soulep
 * Date: Mar 26, 2008
 * Time: 12:38:30 PM
 */
public class PUBProtocol extends Protocol {

   private static String BROKER_URL = "servers";
   private static String TOPIC = "topic";
   private static String INITIAL_CONTEXT_FACTORY = "initialContextFactory";

   private String brokerURL;
   private String topic;
   private String initialContextFactory;

   public PUBProtocol() {
   }


   /**
    * Start the generation.
    */
   public void generate() {
      org.remotej.Compiler.debug("JMS Topic Publish Generation...");
      super.setClientOutputDirectory("publish");
      generateClient();
   }

   private void setupClient(CtClass c) throws CannotCompileException {
      protocolDescription.getProtocolOptions();
      CtField f = CtField.make("private static org.remotej.generator.jms.client.PUBClient pubClient = new org.remotej.generator.jms.client.PUBClient();", c);
      c.addField(f);
      f = CtField.make("private static org.remotej.generator.Transfer transfer = new org.remotej.generator.Transfer();", c);
      c.addField(f);

      String s =
         "public void _setupPUB() { \n" +
            "  pubClient.setBrokerURL(\"" + brokerURL + "\");\n" +
            "  pubClient.setInitialContextFactory(\"" + initialContextFactory + "\");\n" +
            "  pubClient.setTopic(\"" + topic + "\");\n" +
            "}\n";

      Compiler.debug(s);
      CtMethod m = CtNewMethod.make(s, c);
      c.addMethod(m);

      s =
         "public int getExceptionDepth() { \n" +
            "  return pubClient.getExceptionDepth();\n" +
            "}\n";
      Compiler.debug(s);
      m = CtNewMethod.make(s, c);
      c.addMethod(m);

      s =
         "public String getCurrentHost() { \n" +
            "  return pubClient.getCurrentHost();\n" +
            "}\n";
      m = CtNewMethod.make(s, c);
      c.addMethod(m);

      s =
         "public void setHosts(String[] s) { \n" +
            "  pubClient.setHosts(s);\n" +
            "}\n";
      m = CtNewMethod.make(s, c);
      c.addMethod(m);


      CtClass ccc = findClass(pool, "org.remotej.generator.jms.client.PUBClient");
      if (ccc == null) {
         reporter.reportError("class: PUBClient, does not exist.", lineNumber);
         return;
      }
      try {
         ccc.writeFile(clientOutputDirectory + System.getProperty("file.separator") + super.getService());
         //noinspection UnusedAssignment
      } catch (CannotCompileException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      ccc = findClass(pool, "org.remotej.generator.Transfer");
      if (ccc == null) {
         reporter.reportError("class:  Transfer, does not exist.", lineNumber);
         return;
      }
      try {
         ccc.writeFile(clientOutputDirectory + System.getProperty("file.separator") + super.getService());
         //noinspection UnusedAssignment
      } catch (CannotCompileException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void generateClient() {
      addPackages(pool);
      CtClass c;

      // for each matching class / methods
      // generate the code

      HashMap<String, Vector<MethodDescription>> map = protocolDescription.getMethodsByClass();

      if (map.size() != 1 || map.keySet().size() != 1) {
         reporter.reportError("The pub protocol pointcut may only match a single class.method.", lineNumber);
         return;
      }

      // there will only be one
      for (String className : map.keySet()) {
         c = findClass(pool, className);
         if (c == null) {
            reporter.reportError("class: " + className + ", does not exist.", lineNumber);
            return;
         }

         try {
            setupClient(c);
         } catch (CannotCompileException e) {
            reporter.reportError("cannot add JMS publish method to class: " + className, lineNumber);
            return;
         }

         // There will only be one method
         for (MethodDescription md : map.get(className)) {
            CtMethod method;
            try {
               method = getMethod(c, md);
            } catch (NotFoundException e) {
               reporter.reportError("method: " + md.getName() + ", does not exist.", lineNumber);
               return;
            }

            String returnType = checkReturnType(md, method, method.getName());
            if (returnType == null) {
               return;
            }
            if (!"void".equals(returnType)) {
               reporter.reportError("Publish method: " + md.getName() + ", must not have a return value.", lineNumber);
               return;
            }

            try {
               CtClass[] cc = method.getParameterTypes();
               if (cc.length != 1) {
                  reporter.reportError("Publish method,  " + md.getName() + ", must have one parameter.", lineNumber);
                  return;
               }
            } catch (NotFoundException e) {
               reporter.reportError("error retrieving method: " + md.getName() + ", parameters.", lineNumber);
               return;
            }
            
// Check the parameters are serializable
            LinkedList<Parameter> parameters;
            try {
               parameters = createParameters(method);
            } catch (NotFoundException e) {
               reporter.reportError("error reading parameters for method: " + method.getName(), lineNumber);
               return;
            }

            checkParameters(parameters);


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
            String parameterList = makeParameterList(parameters);
            
            // generate the call
            try {
               String methodBody =
                  "public " + returnType + " " + method.getName() + "(" + parameterList + ") throws javax.jms.JMSException {\n" +
                     "  this._setupPUB();\n" +
                     "  transfer.setMethod(\"" + md.getName() + "\");\n" +
                     "  transfer.setClassName(\"" + className + "\");\n" +
                     "  transfer.setCurrentHost(pubClient.getCurrentHost());\n";

               methodBody += setupTransferObject(method, c, parameters);
               methodBody += setupTransferObjectParameters(method, c, parameters);
               
               String lookup =
                  "  pubClient.sendMessage(transfer);\n";


               if (nextServer || continueApp || abort) {
                  methodBody +=
                     "  boolean done = false;\n";
                  methodBody +=
                     "  do {\n" +
                        "     try {\n" +
                        "    " + lookup;
                  if ("void".equals(returnType)) {
                     methodBody += "      return;\n";
                  } else if ("byte".equals(returnType)) {
                     methodBody += "      return ((Byte) res).byteValue();\n";
                  } else if ("short".equals(returnType)) {
                     methodBody += "      return ((Short) res).shortValue();\n";
                  } else if ("int".equals(returnType)) {
                     methodBody += "      return ((Int) res).intValue();\n";
                  } else if ("long".equals(returnType)) {
                     methodBody += "      return ((Long) res).longValue();\n";
                  } else if ("char".equals(returnType)) {
                     methodBody += "      return ((Character) res).charValue();\n";
                  } else if ("float".equals(returnType)) {
                     methodBody += "      return ((Float) res).floatValue();\n";
                  } else if ("double".equals(returnType)) {
                     methodBody += "      return ((Double) res).doubleValue();\n";
                  } else if ("boolean".equals(returnType)) {
                     methodBody += "      return ((Boolean) res).booleanValue();\n";
                  } else {
                     methodBody += "      return (" + returnType + ") res;\n";
                  }

                  methodBody +=
                     "    } catch (Exception e) {\n";
                  if (abort) {
                     methodBody +=
                        "     System.err.println(\"Aborting...\");\n" +
                           "     e.printStackTrace();\n" +
                           "     System.exit(1);\n" +
                           "   }\n" +
                           "} while (!done);\n";
                  } else if (nextServer) {
                     methodBody +=
                        "     e.printStackTrace();\n" +
                           "     pubClient.findAlternateServer();\n" +
                           "    }\n" +
                           "  } while (!done);\n";
                  } else if (continueApp) {
                     methodBody +=
                        "     done = true;" +
                           "   }\n" +
                           "  } while (!done);\n";
                  }

                  if ("void".equals(returnType)) {
                     methodBody += "return;\n}";
                  } else if ("byte".equals(returnType) || "short".equals(returnType) || "int".equals(returnType) ||
                     "long".equals(returnType) || "char".equals(returnType)) {
                     methodBody += "\n  return 0;\n}";
                  } else if ("float".equals(returnType) || "double".equals(returnType)) {
                     methodBody += "\n  return 0.0;\n}";
                  } else if ("boolean".equals(returnType)) {
                     methodBody += "\n  return false;\n}";
                  } else {
                     methodBody += "\n  return null;\n}";
                  }

               } else if (recoveryRoutine) {
                  methodBody += lookup;
                  if ("void".equals(returnType)) {
                     methodBody += "  return;\n}";
                  } else if ("byte".equals(returnType)) {
                     methodBody += "  return ((Byte) res).byteValue();\n}";
                  } else if ("short".equals(returnType)) {
                     methodBody += "  return ((Short) res).shortValue();\n}";
                  } else if ("int".equals(returnType)) {
                     methodBody += "  return ((Int) res).intValue();\n}";
                  } else if ("long".equals(returnType)) {
                     methodBody += "  return ((Long) res).longValue();\n}";
                  } else if ("char".equals(returnType)) {
                     methodBody += "  return ((Character) res).charValue();\n}";
                  } else if ("float".equals(returnType)) {
                     methodBody += "  return ((Float) res).floatValue();\n}";
                  } else if ("double".equals(returnType)) {
                     methodBody += "  return ((Double) res).doubleValue();\n}";
                  } else if ("boolean".equals(returnType)) {
                     methodBody += "  return ((Boolean) res).booleanValue();\n}";
                  } else {
                     methodBody += "  return (" + returnType + ") res;\n}";
                  }
               }

               Compiler.debug(methodBody);

               CtMethod mmm = CtMethod.make(methodBody, c);

               // Add the recovery routine, if it exists
               if (recoveryRoutine) {
                  generateRecoveryRoutine(recoveryName, returnType, mmm);
               }

               c.removeMethod(method);
               c.addMethod(mmm);

            } catch (CannotCompileException e) {
               reporter.reportError("cannot create client method: " + method.getName(), lineNumber);
            } catch (NotFoundException e) {
               reporter.reportError("cannot delete client method: " + method.getName(), lineNumber);
            }
         }
         try {
            c.writeFile(clientOutputDirectory + System.getProperty("file.separator") + super.getService());
            c.detach();
            //noinspection UnusedAssignment
            c = null;
            //noinspection UnusedAssignment
         } catch (CannotCompileException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         }

      }

      return;
   }

   public void validateOptions() throws OptionException {
      brokerURL = protocolOptions.getOptionValue(BROKER_URL);
      topic = protocolOptions.getOptionValue(TOPIC);
      initialContextFactory = protocolOptions.getOptionValue(INITIAL_CONTEXT_FACTORY);

      super.validateOptions();

      if (brokerURL == null) {
         throw new OptionException("brokerURL has not been set");
      }
      if (topic == null) {
         throw new OptionException("Topic has not been set");
      }
      if (initialContextFactory == null) {
         throw new OptionException("initialContextFactory has not been set");
      }

   }

}