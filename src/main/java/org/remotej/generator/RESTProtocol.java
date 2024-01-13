package org.remotej.generator;

import javassist.*;
import org.remotej.Compiler;
import org.remotej.ddl.trees.Parameter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * User: soulep Date: Apr 4, 2008 Time: 10:15:51 AM
 */
public class RESTProtocol extends Protocol {

    private static String SERVERS_URL = "servers";
    private static String SERVER_THREADS = "serverThreads";
    private static String SERVER_PORT = "serverPort";
    public static int DEFAULT_SERVER_PORT = 8888;

    private String serversURL;
    private int serverThreads;
    private int serverPort;

    public RESTProtocol() {
    }

    /**
     * Start the generation.
     */
    public void generate() {
        org.remotej.Compiler.debug("REST Generation...");

        Vector<String> cls = generateClient();
        if (cls != null) {
            generateServer(cls);
        }
    }

    private void setupClient(CtClass c) throws CannotCompileException {
        protocolDescription.getProtocolOptions();
        CtField f = CtField
            .make(
                "private static org.remotej.generator.rest.client.RESTClient restClient = new org.remotej.generator.rest.client.RESTClient();",
                c);
        c.addField(f);
        f = CtField
            .make(
                "private static org.remotej.generator.Transfer transfer = new org.remotej.generator.Transfer();",
                c);
        c.addField(f);

        String s = "public void _setupREST() { \n"
            + "  restClient.setServersURL(\"" + serversURL + "\");\n" + "}\n";
        Compiler.debug(s);
        CtMethod m = CtNewMethod.make(s, c);
        c.addMethod(m);

        s = "public int getExceptionDepth() { \n"
            + "  return restClient.getExceptionDepth();\n" + "}\n";
        Compiler.debug(s);
        m = CtNewMethod.make(s, c);
        c.addMethod(m);

        s = "public String getCurrentHost() { \n"
            + "  return restClient.getCurrentHost();\n" + "}\n";
        m = CtNewMethod.make(s, c);
        c.addMethod(m);

        s = "public void setHosts(String[] s) { \n"
            + "  restClient.setHosts(s);\n" + "}\n";
        m = CtNewMethod.make(s, c);
        c.addMethod(m);

        CtClass ccc = findClass(pool,
            "org.remotej.generator.rest.client.RESTClient");
        if (ccc == null) {
            reporter
                .reportError("class:  RESTClient, does not exist.", lineNumber);
            return;
        }
        try {
            ccc.writeFile(clientOutputDirectory
                + System.getProperty("file.separator") + super.getService());
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
            ccc.writeFile(clientOutputDirectory
                + System.getProperty("file.separator") + super.getService());
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Vector<String> generateClient() {
        Vector<String> v = new Vector<String>();

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
                reporter.reportError("cannot add REST methods to class: "
                    + className, lineNumber);
                return null;
            }

            v.add(c.getName());

            for (MethodDescription md : map.get(className)) {
                CtMethod method;
                try {
                    method = getMethod(c, md);
                } catch (NotFoundException e) {
                    reporter.reportError("method: " + md.getName()
                        + ", does not exist.", lineNumber);
                    return null;
                }

                super.checkSynchronized(method);
                String returnType = checkReturnType(md, method, method.getName());

                // store fully qualified return type
                md.getReturnValue().returnValue.spelling = returnType;

                LinkedList<Parameter> parameters = md.getParameters().parameters;
                try {
                    super.expandParameters(parameters);
                } catch (NotFoundException e) {
                    reporter.reportError("error reading parameters for method: "
                        + method.getName(), lineNumber);
                    return null;
                }

                String parameterList = makeParameterList(parameters);

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
                    String methodBody = "public " + returnType + " "
                        + method.getName() + "(" + parameterList
                        + ") throws Exception {\n" + "  this._setupREST();\n";

                    methodBody += setupTransferObject(method, c, parameters);
                    methodBody += setupTransferObjectParameters(method, c, parameters);

                    String lookup =
                        "  org.remotej.generator.Transfer res = restClient.sendMessage(transfer);\n";

                    if (nextServer || continueApp || abort) {
                        methodBody += "  boolean done = false;\n";
                        methodBody += "  do {\n" + "     try {\n" + "    " + lookup;
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
                            methodBody += "      return (" + returnType
                                + ") res.getReturnValue();\n";
                        }

                        methodBody += "    } catch (Exception e) {\n";
                        if (abort) {
                            methodBody += "     System.err.println(\"Aborting...\");\n"
                                + "     e.printStackTrace();\n"
                                + "     System.exit(1);\n"
                                + "   }\n"
                                + "} while (!done);\n";
                        } else if (nextServer) {
                            methodBody += "     e.printStackTrace();\n"
                                + "     restClient.findAlternateServer();\n"
                                + "    }\n" + "  } while (!done);\n";
                        } else if (continueApp) {
                            methodBody += "     done = true;" + "   }\n"
                                + "  } while (!done);\n";
                        }

                        if ("void".equals(returnType)) {
                            methodBody += "\n}";
                        } else if ("byte".equals(returnType)
                            || "short".equals(returnType)
                            || "int".equals(returnType)
                            || "long".equals(returnType)
                            || "char".equals(returnType)) {
                            methodBody += "\n  return 0;\n}";
                        } else if ("float".equals(returnType)
                            || "double".equals(returnType)) {
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
                            methodBody += "  return (" + returnType
                                + ") res.getReturnValue();\n}";
                        }
                    }

                    Compiler.debug(methodBody);
                    CtMethod mmm = CtNewMethod.make(methodBody, c);

                    // // Add the recovery routine, if it exists
                    if (recoveryRoutine) {
                        generateRecoveryRoutine(recoveryName, returnType, mmm);
                    }

                    c.removeMethod(method);
                    c.addMethod(mmm);

                } catch (CannotCompileException e) {
                    reporter.reportError("cannot create client method: "
                        + method.getName(), lineNumber);
                    System.err.println(e);
                } catch (NotFoundException e) {
                    reporter.reportError("cannot delete client method: "
                        + method.getName(), lineNumber);
                }
            }

            try {
                c.writeFile(clientOutputDirectory
                    + System.getProperty("file.separator") + super.getService());
                c.detach();
                c = null;
            } catch (CannotCompileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return v;
    }

    public void generateServer(Vector<String> cls) {
        String main = "public static void main(String[] args) throws Exception {\n";

        if (serverPlugin != null) {
            main += "new " + serverPlugin + "().start();\n";
        }

        main += "  org.remotej.server.RESTServer s = new org.remotej.server.RESTServer();\n"
            + "  s.setServerThreads("
            + serverThreads
            + ");\n"
            + "  s.setPort("
            + serverPort + ");\n" + "  s.startServer();\n" + "}\n";
        Compiler.debug(main);
        String className = "org.remotej.server.RESTServer";
        String resource = "org.remotej.server.RemoteJResource";
        String transfer = "org.remotej.generator.Transfer";

        CtClass c = findClass(pool, className);

        if (c == null) {
            reporter.reportError("class: " + className + ", does not exist.",
                lineNumber);
            return;
        }

        CtClass tr = findClass(pool, transfer);
        if (tr == null) {
            reporter.reportError("class: " + transfer + ", does not exist.",
                lineNumber);
            return;
        }

        CtClass res = findClass(pool, resource);
        if (tr == null) {
            reporter.reportError("class: " + resource + ", does not exist.",
                lineNumber);
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
            c.removeMethod(method);
            CtMethod mmm = CtMethod.make(main, c);
            c.addMethod(mmm);
            c.writeFile(serverOutputDirectory
                + System.getProperty("file.separator") + super.getService());
            tr.writeFile(serverOutputDirectory
                + System.getProperty("file.separator") + super.getService());
            res.writeFile(serverOutputDirectory
                + System.getProperty("file.separator") + super.getService());
            c.detach();
            tr.detach();
            res.detach();
            // noinspection UnusedAssignment
            c = null;
            tr = null;
            res = null;
            // noinspection UnusedAssignment
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void validateOptions() throws OptionException {
        serversURL = protocolOptions.getOptionValue(SERVERS_URL);

        super.validateOptions();

        if (protocolOptions.getOptionValue(SERVER_THREADS) == null) {
            serverThreads = 1;
        } else {
            try {
                serverThreads = Integer.parseInt(protocolOptions
                    .getOptionValue(SERVER_THREADS));
            } catch (NumberFormatException e) {
                throw new OptionException(SERVER_THREADS
                    + " option does not contain an integer value",
                    protocolOptions.getLineNo(SERVER_THREADS));
            }
        }

        if (protocolOptions.getOptionValue(SERVER_PORT) == null) {
            serverPort = DEFAULT_SERVER_PORT;
        } else {
            try {
                serverPort = Integer.parseInt(protocolOptions
                    .getOptionValue(SERVER_PORT));
            } catch (NumberFormatException e) {
                throw new OptionException(SERVER_PORT
                    + " option does not contain an integer value",
                    protocolOptions.getLineNo(SERVER_PORT));
            }
        }

        if (serversURL == null) {
            throw new OptionException("REST servers have not been set");
        }

        // add default port if it's not there
        StringTokenizer st = new StringTokenizer(serversURL, ",");
        int i = 0;
        String[] hosts = new String[st.countTokens()];
        while (st.hasMoreTokens()) {
            hosts[i] = st.nextToken();
            try {
                URL u = new URI(hosts[i]).toURL();
                if (u.getPort() == -1) {
                    u = new URI(u.getProtocol(), null, u.getHost(), serverPort, u
                        .getFile(), null, null).toURL();
                }
                hosts[i] = u.toExternalForm() + "/" + super.getService();
            } catch (MalformedURLException e) {
                throw new OptionException(SERVERS_URL
                    + " option containns an invalid URL", protocolOptions
                    .getLineNo(SERVERS_URL));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            i++;
        }
        serversURL = "";
        for (int j = 0; j < hosts.length; j++) {
            serversURL += hosts[j];
            if (j != hosts.length - 1) {
                serversURL += ",";
            }
        }

    }

}
