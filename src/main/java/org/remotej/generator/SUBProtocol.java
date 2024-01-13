package org.remotej.generator;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.remotej.Compiler;
import org.remotej.ddl.trees.Parameter;

/**
 * Copyright(c) Paul Soule. All rights reserved.
 * <p/>
 * This file is part of the RemoteJ system.
 * <p/>
 * Created at 3:28:26 PM on Mar 26, 2006
 */
public final class SUBProtocol extends Protocol {

   private static String BROKER_URL = "servers";
   private static String TOPIC = "topic";
   private static String INITIAL_CONTEXT_FACTORY = "initialContextFactory";
   private static String DURABLE = "durable";
   private static String SUBSCRIBER = "subscriber";
   private static String CLIENT_ID = "clientID";
   private static String RECEIVE_TIMEOUT = "receiveTimeout";

   private String brokerURL;
   private String topic;
   private String initialContextFactory;
   private int receiveTimeout;
   public boolean durable;
   public String subscriberName;
   public String clientID;

   public SUBProtocol() {
   }


   /**
    * Start the generation.
    */
   public void generate() {
      Compiler.debug("JMS Topic Subscribe Generation...");
      super.setClientOutputDirectory("subscribe");
      generateClient();
   }

   private void setupClient(CtClass c, String target, String parameterType) throws CannotCompileException {
      protocolDescription.getProtocolOptions();
      CtField f = CtField.make("private static org.remotej.generator.jms.client.SUBClient subClient = new org.remotej.generator.jms.client.SUBClient();", c);
      c.addField(f);
      f = CtField.make("private static org.remotej.generator.Transfer transfer = new org.remotej.generator.Transfer();", c);
      c.addField(f);

      String s =
         "public void _setupSUB() { \n" +
            "  subClient.setTimeout(" + receiveTimeout + ");\n" +
            "  subClient.setBrokerURL(\"" + brokerURL + "\");\n" +
            "  subClient.setInitialContextFactory(\"" + initialContextFactory + "\");\n" +
            "  subClient.setTopic(\"" + topic + "\");\n" +
            "  subClient.setDurable(" + durable + ");\n" +
            "  subClient.setSubscriberName(\"" + subscriberName + "\");\n" +
            "  subClient.setClientID(\"" + clientID + "\");" +
            "  subClient.setListener(this);\n" +
            "}\n";

      Compiler.debug(s);
      CtMethod m = CtNewMethod.make(s, c);
      c.addMethod(m);

      CtConstructor constructor = null;
      try {
         constructor = c.getConstructor("()V");
      } catch (NotFoundException e) {
      }
      
      if (constructor == null) {
         constructor = new CtConstructor(null, c);
      }
      
      String methodBody =
         "    _setupSUB();\n" +
            "  transfer.setMethod(\"" + target + "\");\n" +
            "  transfer.setClassName(\"" + c.getName() + "\");\n" +
            "  transfer.setCurrentHost(subClient.getCurrentHost());\n" +
            "  try {\n" +
            "    subClient.getConnection();\n" +
            "  } catch (Exception e) { \n" +
            "  }\n";
      
      constructor.insertAfter(methodBody);

      s =
         "public int getExceptionDepth() { \n" +
            "  return subClient.getExceptionDepth();\n" +
            "}\n";
      Compiler.debug(s);
      m = CtNewMethod.make(s, c);
      c.addMethod(m);

      s =
         "public String getCurrentHost() { \n" +
            "  return subClient.getCurrentHost();\n" +
            "}\n";
      m = CtNewMethod.make(s, c);
      c.addMethod(m);

      s =
         "public void setHosts(String[] s) { \n" +
            "  subClient.setHosts(s);\n" +
            "}\n";
      m = CtNewMethod.make(s, c);
      c.addMethod(m);

      CtClass ccc = findClass(pool, "org.remotej.generator.jms.client.SUBClient");
      if (ccc == null) {
         reporter.reportError("class:  SUBClient, does not exist.", lineNumber);
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

      HashMap<String, Vector<MethodDescription>> map = protocolDescription.getMethodsByClass();

      if (map.size() != 1 || map.keySet().size() != 1) {
         reporter.reportError("The sub protocol pointcut may only match a single class.method.", lineNumber);
         return;
      }

      String className = map.keySet().iterator().next();
      MethodDescription md = map.get(className).iterator().next();

      c = findClass(pool, className);
      if (c == null) {
         reporter.reportError("class: " + className + ", does not exist.", lineNumber);
         return;
      }

      CtClass messageListener = findClass(pool, "javax.jms.MessageListener");
      if (messageListener == null) {
         reporter.reportError("Cannot find the javax.jms.MessageListener class.", lineNumber);
         return;
      }
      super.alterToImplementInterface(c, messageListener);

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
         reporter.reportError("Subscribe method: " + md.getName() + ", must not have a return value.", lineNumber);
         return;
      }

      String parameterType;
      try {
         CtClass[] cc = method.getParameterTypes();
         if (cc.length != 1) {
            reporter.reportError("Publish method,  " + md.getName() + ", must have one parameter.", lineNumber);
            return;
         }
         if (cc[0].isPrimitive()) {
            reporter.reportError("Parameter cannot be a primitive type.", lineNumber);
            return;
         }
         LinkedList<Parameter> parameters;
         try {
            parameters = createParameters(method);
         } catch (NotFoundException e) {
            reporter.reportError("error reading parameters for method: " + method.getName(), lineNumber);
            return;
         }
         // check if parameters are Serializable
         checkParameters(parameters);
         parameterType = cc[0].getName();
      } catch (NotFoundException e) {
         reporter.reportError("error retrieving method: " + md.getName() + ", parameters.", lineNumber);
         return;
      }

      try {
         setupClient(c, md.getName(), parameterType);
      } catch (CannotCompileException e) {
         reporter.reportError("cannot add JMS subscribe methods to class: " + className, lineNumber);
         return;
      }

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
      // alter the static initializer to setup the event
      try {
         CtConstructor constructor = c.getClassInitializer();
         if (constructor == null) {
            constructor = c.makeClassInitializer();
         }

         String methodBody = "public void onMessage(javax.jms.Message message) {\n";

         String lookup =
            "\t\tjavax.jms.ObjectMessage objMsg = (javax.jms.ObjectMessage) message;\n" +
            "\t\t" + parameterType + "  obj = (" + parameterType + ") objMsg.getObject();\n" +
            "\t\t" + md.getName() + "(obj);\n";


         if (nextServer || continueApp || abort) {
            methodBody +=
               "  boolean done = false;\n";
            methodBody +=
               "  do {\n" +
                  "     try {\n" +
                  "    " + lookup;
            methodBody += "      return;\n";


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
                     "     subClient.findAlternateServer();\n" +
                     "    }\n" +
                     "  } while (!done);\n";
            } else if (continueApp) {
               methodBody +=
                  "     done = true;" +
                     "   }\n" +
                     "  } while (!done);\n";
            }

         } else if (recoveryRoutine) {
            methodBody += lookup;
         }

         methodBody +="}\n";

         Compiler.debug(methodBody);

         CtMethod mmm = CtMethod.make(methodBody, c);

         // Add the recovery routine, if it exists
         if (recoveryRoutine) {
            generateRecoveryRoutine(recoveryName, returnType, mmm);
         }

         c.addMethod(mmm);

      } catch (CannotCompileException e) {
         reporter.reportError("cannot create client method: " + method.getName(), lineNumber);
      //} catch (NotFoundException e) {
      //   reporter.reportError("cannot delete client method: " + method.getName(), lineNumber);
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

   public void validateOptions() throws OptionException {
      brokerURL = protocolOptions.getOptionValue(BROKER_URL);
      topic = protocolOptions.getOptionValue(TOPIC);
      initialContextFactory = protocolOptions.getOptionValue(INITIAL_CONTEXT_FACTORY);

      super.validateOptions();

      String b = protocolOptions.getOptionValue(DURABLE);
      if (b == null) {
         b = "false";
      }

      if (!"true".equals(b) && !"false".equals(b)) {
         throw new OptionException("true or false expected for " + DURABLE + " option",
            protocolOptions.getLineNo(DURABLE));
      }

      durable =  Boolean.parseBoolean(protocolOptions.getOptionValue(DURABLE));

      if (durable) {
         subscriberName = protocolOptions.getOptionValue(SUBSCRIBER);
         clientID = protocolOptions.getOptionValue(CLIENT_ID);
         if (subscriberName == null || clientID == null) {
            throw new OptionException("durable is set but the clientID or subscriber has not been set.",
               protocolOptions.getLineNo(DURABLE));
         }
      }


      if (protocolOptions.getOptionValue(RECEIVE_TIMEOUT) == null) {
         receiveTimeout = 0;
      } else {
         try {
            receiveTimeout =  Integer.parseInt(protocolOptions.getOptionValue(RECEIVE_TIMEOUT));
         } catch (NumberFormatException e) {
            throw new OptionException(RECEIVE_TIMEOUT + " option does not contain an integer value",
               protocolOptions.getLineNo(RECEIVE_TIMEOUT));
         }
      }

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
