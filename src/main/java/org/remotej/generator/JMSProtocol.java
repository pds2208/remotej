package org.remotej.generator;

import javassist.*;
import org.remotej.Compiler;
import org.remotej.ddl.trees.Parameter;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Copyright(c) Paul Soule. All rights reserved.
 * <p/>
 * This file is part of the RemoteJ system.
 * <p/>
 * Created at 3:28:26 PM on Mar 26, 2006
 */
public final class JMSProtocol extends Protocol {

   private static String BROKER_URL = "servers";
   private static String SEND_QUEUE = "sendQueue";
   private static String RECEIVE_QUEUE = "receiveQueue";
   private static String SERVER_THREADS = "serverThreads";
   private static String INITIAL_CONTEXT_FACTORY = "initialContextFactory";
   private static String PERSIST = "persist";
   private static String RECEIVE_TIMEOUT = "receiveTimeout";

   private String brokerURL;
   private String sendQueue;
   private String receiveQueue;
   private int serverThreads;
   private String initialContextFactory;
   private boolean persist;
   private int receiveTimeout;

   public JMSProtocol() {
   }


   /**
    * Start the generation.
    */
   public void generate() {
      Compiler.debug("JMS Generation...");

      Vector<String> cls = generateClient();
      if (cls != null) {
         generateServer(cls);
      }

   }

   private void setupClient(CtClass c) throws CannotCompileException {
      protocolDescription.getProtocolOptions();
      CtField f = CtField.make("private static org.remotej.generator.jms.client.JMSClient jmsClient = new org.remotej.generator.jms.client.JMSClient();", c);
      c.addField(f);
      f = CtField.make("private static org.remotej.generator.Transfer transfer = new org.remotej.generator.Transfer();", c);
      c.addField(f);

      String s =
         "public void _setupJMS() { \n" +
            "  jmsClient.setTimeout(" + receiveTimeout + ");\n" +
            "  jmsClient.setBrokerURL(\"" + brokerURL + "\");\n" +
            "  jmsClient.setInitialContextFactory(\"" + initialContextFactory + "\");\n" +
            "  jmsClient.setSendQueue(\"" + sendQueue + "\");\n" +
            "  jmsClient.setPersist(\"" + persist + "\");\n" +
            "  jmsClient.setReceiveQueue(\"" + receiveQueue + "\");\n" +
            "}\n";
      Compiler.debug(s);
      CtMethod m = CtNewMethod.make(s, c);
      c.addMethod(m);

      s =
         "public int getExceptionDepth() { \n" +
            "  return jmsClient.getExceptionDepth();\n" +
            "}\n";
      Compiler.debug(s);
      m = CtNewMethod.make(s, c);
      c.addMethod(m);

      s =
         "public String getCurrentHost() { \n" +
            "  return jmsClient.getCurrentHost();\n" +
            "}\n";
      m = CtNewMethod.make(s, c);
      c.addMethod(m);

      s =
         "public void setHosts(String[] s) { \n" +
            "  jmsClient.setHosts(s);\n" +
            "}\n";
      m = CtNewMethod.make(s, c);
      c.addMethod(m);


      CtClass ccc = findClass(pool, "org.remotej.generator.jms.client.JMSClient");
      if (ccc == null) {
         reporter.reportError("class:  JMSClient, does not exist.", lineNumber);
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

   public Vector<String> generateClient() {
      Vector<String> v = new Vector<String>();

      addPackages(pool);

      CtClass c = null;

      // for each matching class / methods
      // generate the code


      HashMap<String, Vector<MethodDescription>> map = protocolDescription.getMethodsByClass();

      for (String className : map.keySet()) {

         c = findClass(pool, className);
         if (c == null) {
            reporter.reportError("class: " + className + ", does not exist.", lineNumber);
            return null;
         }

         if (!super.hasNullConstructor(c)) {
            reporter.reportError("class: " + c.getName() +
               ", does not have a null constructor.", lineNumber);
            return null;
         }

         try {
            setupClient(c);
         } catch (CannotCompileException e) {
            reporter.reportError("cannot add JMS methods to class: " + className, lineNumber);
            return null;
         }

         v.add(c.getName());

         for (MethodDescription md : map.get(className)) {
            CtMethod method;
            try {
               method = getMethod(c, md);
            } catch (NotFoundException e) {
               reporter.reportError("method: " + md.getName() + ", does not exist.", lineNumber);
               return null;
            }

            super.checkSynchronized(method);

            String returnType = checkReturnType(md, method, method.getName());
            if (returnType == null) {
               return null;
            }
            // Check the parameters are serializable
            LinkedList<Parameter> parameters;
            try {
               parameters = createParameters(method);
            } catch (NotFoundException e) {
               reporter.reportError("error reading parameters for method: " + method.getName(), lineNumber);
               return null;
            }
            // Build up parameter string
            String parameterList = makeParameterList(parameters);
            makeParameterValues(parameters);
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
               String methodBody =
                  "public " + returnType + " " + method.getName() + "(" + parameterList + ") throws javax.jms.JMSException {\n" +
                     "  this._setupJMS();\n";

               methodBody += setupTransferObject(method, c, parameters);
               methodBody += setupTransferObjectParameters(method, c, parameters);

               String lookup =
                  "  org.remotej.generator.Transfer res = jmsClient.sendMessage(transfer);\n";


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
                     methodBody += "      return ((Byte) res.getReturnValue()).byteValue();\n";
                  } else if ("short".equals(returnType)) {
                     methodBody += "      return ((Short) res.getReturnValue()).shortValue();\n";
                  } else if ("int".equals(returnType)) {
                     methodBody += "      return ((Int) res.getReturnValue()).intValue();\n";
                  } else if ("long".equals(returnType)) {
                     methodBody += "      return ((Long) res.getReturnValue()).longValue();\n";
                  } else if ("char".equals(returnType)) {
                     methodBody += "      return ((Character) res.getReturnValue()).charValue();\n";
                  } else if ("float".equals(returnType)) {
                     methodBody += "      return ((Float) res.getReturnValue()).floatValue();\n";
                  } else if ("double".equals(returnType)) {
                     methodBody += "      return ((Double) res.getReturnValue()).doubleValue();\n";
                  } else if ("boolean".equals(returnType)) {
                     methodBody += "      return ((Boolean) res.getReturnValue()).booleanValue();\n";
                  } else {
                     methodBody += "      return (" + returnType + ") res.getReturnValue();\n";
                  }

                  methodBody +=
                     "    } catch (javax.jms.JMSException e) {\n";
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
                           "     jmsClient.findAlternateServer();\n" +
                           "    }\n" +
                           "  } while (!done);\n";
                  } else if (continueApp) {
                     methodBody +=
                        "     done = true;" +
                           "   }\n" +
                           "  } while (!done);\n";
                  }

                  if ("void".equals(returnType)) {
                     methodBody += "\n}";
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
                     methodBody += "  return ((Byte) res.getReturnValue()).byteValue();\n}";
                  } else if ("short".equals(returnType)) {
                     methodBody += "  return ((Short) res.getReturnValue()).shortValue();\n}";
                  } else if ("int".equals(returnType)) {
                     methodBody += "  return ((Int) res.getReturnValue()).intValue();\n}";
                  } else if ("long".equals(returnType)) {
                     methodBody += "  return ((Long) res.getReturnValue()).longValue();\n}";
                  } else if ("char".equals(returnType)) {
                     methodBody += "  return ((Character) res.getReturnValue()).charValue();\n}";
                  } else if ("float".equals(returnType)) {
                     methodBody += "  return ((Float) res.getReturnValue()).floatValue();\n}";
                  } else if ("double".equals(returnType)) {
                     methodBody += "  return ((Double) res.getReturnValue()).doubleValue();\n}";
                  } else if ("boolean".equals(returnType)) {
                     methodBody += "  return ((Boolean) res.getReturnValue()).booleanValue();\n}";
                  } else {
                     methodBody += "  return (" + returnType + ") res.getReturnValue();\n}";
                  }
               }

               Compiler.debug(methodBody);

               CtMethod mmm = CtNewMethod.make(methodBody, c);

//               // Add the recovery routine, if it exists
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


      return v;
   }

   public void generateServer(Vector<String> cls) {
      String main =
         "public static void main(String[] args) throws  javax.naming.NamingException {\n";

      if (serverPlugin != null) {
         main += "new " + serverPlugin + "().start();\n";
      }

      main +=
         "  org.remotej.server.JMSServer s = new org.remotej.server.JMSServer();\n" +
            "  s.setServerThreads(" + serverThreads + ");\n" +
            "  s.setInitialContextFactory(\"" + initialContextFactory + "\");\n" +
            "  s.setBrokerURL(\"" + brokerURL + "\");\n" +
            "  s.setDestinationQueueName(\"" + sendQueue + "\");\n" +
            "  s.setPersist(\"" + persist + "\");\n" +
            "  s.setupContainer();\n" +
            "  s.start();\n" +
            "}\n";
      Compiler.debug(main);
      String className = "org.remotej.server.JMSServer";
      String transfer = "org.remotej.generator.Transfer";
      CtClass c = findClass(pool, className);

      if (c == null) {
         reporter.reportError("class: " + className + ", does not exist.", lineNumber);
         return;
      }

      CtClass tr = findClass(pool, transfer);
      if (tr == null) {
         reporter.reportError("class: " + transfer + ", does not exist.", lineNumber);
         return;
      }

      CtMethod method;
      try {
         method = getMethodByName(c, "main");
      } catch (NotFoundException e) {
         reporter.reportError("Cannnot find main method.", lineNumber);
         return;
      }

      try {
         CtMethod mmm = CtMethod.make(main, c);
         c.removeMethod(method);
         c.addMethod(mmm);
         c.writeFile(serverOutputDirectory + System.getProperty("file.separator") + super.getService());
         tr.writeFile(serverOutputDirectory + System.getProperty("file.separator") + super.getService());
         c.detach();
         tr.detach();
         //noinspection UnusedAssignment
         c = null;
         tr = null;
         //noinspection UnusedAssignment
      } catch (NotFoundException e) {
         e.printStackTrace();
      } catch (CannotCompileException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   public void validateOptions() throws OptionException {
      brokerURL = protocolOptions.getOptionValue(BROKER_URL);
      sendQueue = protocolOptions.getOptionValue(SEND_QUEUE);
      receiveQueue = protocolOptions.getOptionValue(RECEIVE_QUEUE);
      initialContextFactory = protocolOptions.getOptionValue(INITIAL_CONTEXT_FACTORY);

      super.validateOptions();

      String b = protocolOptions.getOptionValue(PERSIST);
      if (b == null) {
         b = "false";
      }

      if (!"true".equals(b) && !"false".equals(b)) {
         throw new OptionException("true or false expected for " + PERSIST + " option",
            protocolOptions.getLineNo(PERSIST));
      }

      persist = Boolean.parseBoolean(protocolOptions.getOptionValue(PERSIST));

      if (protocolOptions.getOptionValue(RECEIVE_TIMEOUT) == null) {
         receiveTimeout = 0;
      } else {
         try {
            receiveTimeout = Integer.parseInt(protocolOptions.getOptionValue(RECEIVE_TIMEOUT));
         } catch (NumberFormatException e) {
            throw new OptionException(RECEIVE_TIMEOUT + " option does not contain an integer value",
               protocolOptions.getLineNo(RECEIVE_TIMEOUT));
         }
      }

      if (protocolOptions.getOptionValue(SERVER_THREADS) == null) {
         serverThreads = 1;
      } else {
         try {
            serverThreads = Integer.parseInt(protocolOptions.getOptionValue(SERVER_THREADS));
         } catch (NumberFormatException e) {
            throw new OptionException(SERVER_THREADS + " option does not contain an integer value",
               protocolOptions.getLineNo(SERVER_THREADS));
         }
      }
      if (brokerURL == null) {
         throw new OptionException("brokerURL has not been set");
      }
      if (sendQueue == null) {
         throw new OptionException("sendQueue has not been set");
      }
      if (receiveQueue == null) {
         throw new OptionException("receiveQueue has not been set");
      }
      if (serverThreads == 0) {
         serverThreads = 1;
      }
      if (initialContextFactory == null) {
         throw new OptionException("initialContextFactory has not been set");
      }

   }

}
