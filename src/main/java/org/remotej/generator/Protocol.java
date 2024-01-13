package org.remotej.generator;

import javassist.*;
import javassist.bytecode.*;
import org.remotej.Compiler;
import org.remotej.ddl.ErrorHandler;
import org.remotej.ddl.analyser.SourcePosition;
import org.remotej.ddl.trees.Identifier;
import org.remotej.ddl.trees.Parameter;
import org.remotej.ddl.trees.ParameterDeclaration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

public abstract class Protocol implements IProtocol {

    /**
     * The service description containing info on the pointcuts, recovery
     * routines etc.
     */

    private ServiceDescription serviceDescription;

    /**
     * List of all recovery routines
     */

    private Vector<JavaMethod> recovery;

    /**
     * import statements in the DDL
     */
    private Vector<String> imports;

    /**
     * Options for all defined protocols
     */
    protected ProtocolOptions protocolOptions;

    /**
     * Current position in the DDL
     */
    protected SourcePosition lineNumber;

    /**
     * Compilers error handler
     */
    protected ErrorHandler reporter;

    /**
     * Location of the server output files
     */
    protected String serverOutputDirectory = "";

    /**
     * Location of the client output files
     */
    protected String clientOutputDirectory = "";

    /**
     * Description of all protocols
     */
    protected ProtocolDescription protocolDescription;

    /**
     * Javassist's class pool
     */
    protected ClassPool pool = ClassPool.getDefault();

    /**
     * The name of the service
     */
    private String service;

    /**
     * The server plugin option
     */
    private static String SERVER_PLUGIN = "serverPlugin";
    protected String serverPlugin;

    public Protocol() {
    }

    /**
     * Validate protocol options
     *
     * @throws OptionException if there is a problem validating options
     */
    @SuppressWarnings("unchecked")
    public void validateOptions() throws OptionException {
        serverPlugin = protocolOptions.getOptionValue(SERVER_PLUGIN);
        if (serverPlugin != null) {
            try {
                Class c = Class.forName(serverPlugin);
                if (!Thread.class.isAssignableFrom(c)) {
                    throw new OptionException("Server plugin: " + serverPlugin
                        + ", must extend the Thread class");
                }
            } catch (ClassNotFoundException e) {
                throw new OptionException("Cannot instantiate server plugin: "
                    + serverPlugin);
            }
        }
    }

    /**
     * Get the protocol description
     *
     * @return the protocol description
     */
    public ProtocolDescription getProtocolDescription() {
        return protocolDescription;
    }

    /**
     * Set the protocol description
     *
     * @param pointcutDescription The protocol description
     */
    public void setProtocolDescription(ProtocolDescription pointcutDescription) {
        this.protocolDescription = pointcutDescription;
    }

    /**
     * Get the service dscription
     *
     * @return the service description
     */
    public final ServiceDescription getServiceDescription() {
        return serviceDescription;
    }

    /**
     * Set the service description
     *
     * @param serviceDescription the service description
     */
    public final void setServiceDescription(ServiceDescription serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public final Vector<JavaMethod> getRecovery() {
        return recovery;
    }

    public final void setRecovery(Vector<JavaMethod> recovery) {
        this.recovery = recovery;
    }

    public final ErrorHandler getReporter() {
        return reporter;
    }

    public final void setReporter(ErrorHandler reporter) {
        this.reporter = reporter;
    }

    public final SourcePosition getLineNumber() {
        return lineNumber;
    }

    public final void setLineNumber(SourcePosition lineNumber) {
        this.lineNumber = lineNumber;
    }

    protected final ProtocolOptions getOptions() {
        return protocolOptions;
    }

    public final void setOptions(ProtocolOptions protocolOptions) {
        this.protocolOptions = protocolOptions;
    }

    protected final String getBaseClassName(String s) {
        return s.substring(s.lastIndexOf('.') + 1, s.length());
    }

    public final void setImports(Vector<String> imports) {
        this.imports = imports;
        imports.insertElementAt("java.lang.*", 0);
    }

    /**
     * Find a class in a pool given its name. Searches along the imports list
     *
     * @param pool      The pool
     * @param className The class to find
     * @return the class representation
     */
    protected final CtClass findClass(ClassPool pool, String className) {
        // try the name as is
        // noinspection EmptyCatchBlock
        try {
            return pool.get(className);
        } catch (NotFoundException e) {
        }

        for (String s : imports) {
            int i = s.lastIndexOf('*'); // wildcard import
            if (i > 0) {
                s = s.substring(0, i - 1);
            } else {
                s = s.substring(0, s.lastIndexOf('.'));
            }
            try {
                return pool.get(s + "." + className);
            } catch (NotFoundException ee) {
            }
        }

        return null;
    }

    /**
     * Find a class in a pool given its name. Searches along the imports list
     *
     * @param pool      The pool
     * @param className The class to find
     * @param newName
     * @return the class representation
     */
    protected final CtClass findAndRenameClass(ClassPool pool, String className,
                                               String newName) {
        try {
            return pool.getAndRename(className, newName);
        } catch (NotFoundException e) {
        }

        for (String s : imports) {
            int i = s.lastIndexOf('*'); // wildcard import
            if (i > 0) {
                s = s.substring(0, i - 1);
            } else {
                s = s.substring(0, s.lastIndexOf('.'));
            }
            try {
                return pool.getAndRename(s + "." + className, newName);
            } catch (NotFoundException ee) {
            }
        }

        return null;
    }

    /**
     * Get the servers output directory
     *
     * @return the output directory
     */
    public final String getServerOutputDirectory() {
        return serverOutputDirectory;
    }

    /**
     * Set the output directory
     *
     * @param outputDirectory the output directory
     */
    public final void setServerOutputDirectory(String outputDirectory) {
        this.serverOutputDirectory = outputDirectory;
    }

    public final String getClientOutputDirectory() {
        return clientOutputDirectory;
    }

    public final void setClientOutputDirectory(String outputDirectory) {
        this.clientOutputDirectory = outputDirectory;
    }

    /**
     * Generate protocol
     */
    public void generateAll() {
        ClassPool pool = ClassPool.getDefault();

        Vector<MethodDescription> methods = protocolDescription.getMethods();
        Vector<MethodDescription> expandedMethods = new Vector<MethodDescription>();
        HashMap<String, Vector<MethodDescription>> map = new HashMap<String, Vector<MethodDescription>>();

        // expand wildcards
        for (MethodDescription m : methods) {
            String returnVal = m.getReturnValue().returnValue.spelling;
            String methodName = m.getName();
            String className = m.getClassName();
            CtClass c = findClass(pool, className);
            ParameterDeclaration pd = m.getParameters();

            if (c == null) {
                reporter.reportError("class: " + className + ", does not exist.",
                    lineNumber);
                return;
            }

            assert c != null;

            CtMethod[] cm;
            try {
                cm = c.getMethods();
            } catch (java.lang.RuntimeException e) {
                reporter.reportError("class: " + className + ", does not exist.",
                    lineNumber);
                return;
            }
            boolean found = false;
            for (CtMethod aCm : cm) {
                String mName = aCm.getName();
                if ("equals".equals(mName) || "wait".equals(mName)
                    || "toString".equals(mName) || "notifyAll".equals(mName)
                    || "notify".equals(mName) || "hashCode".equals(mName)
                    || "getClass".equals(mName) || "finalize".equals(mName)
                    || "clone".equals(mName)) {

                    continue;
                }

                boolean matchMethod = matchMethodOrReturn(methodName, aCm.getName());
                boolean matchReturn = false;
                boolean matchParameters = false;

                try {
                    CtClass ct = aCm.getReturnType();
                    String s = aCm.getReturnType().getName();
                    if (ct.equals(CtClass.booleanType)) {
                        s = "boolean";
                    } else if (ct.equals(CtClass.byteType)) {
                        s = "byte";
                    } else if (ct.equals(CtClass.charType)) {
                        s = "char";
                    } else if (ct.equals(CtClass.doubleType)) {
                        s = "double";
                    } else if (ct.equals(CtClass.floatType)) {
                        s = "float";
                    } else if (ct.equals(CtClass.intType)) {
                        s = "int";
                    } else if (ct.equals(CtClass.longType)) {
                        s = "long";
                    } else if (ct.equals(CtClass.shortType)) {
                        s = "short";
                    } else if (ct.equals(CtClass.voidType)) {
                        s = "void";
                    } else {
                        CtClass returnClass = findClass(pool, returnVal);
                        if (returnClass != null) {
                            returnVal = returnClass.getName();
                        }
                    }
                    matchReturn = matchMethodOrReturn(returnVal, s);
                    matchParameters = matchParameters(pd, aCm.getParameterTypes());
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }

                if (matchMethod && matchReturn && matchParameters) {
                    found = true;
                    MethodDescription md = new MethodDescription();
                    md.setClassName(className);
                    md.setName(aCm.getName());
                    md.setParameters(pd);
                    md.setRecoveryName(m.getRecoveryName());

                    try {
                        md.setReturnValue(m.getReturnValue());
                        md.getReturnValue().returnValue.spelling = aCm
                            .getReturnType().getSimpleName();
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    }

                    Vector<MethodDescription> vd;
                    if (map.containsKey(className)) {
                        vd = map.get(className);
                    } else {
                        vd = new Vector<MethodDescription>();
                    }
                    vd.add(md);
                    map.put(className, vd);

                    expandedMethods.add(md);
                }
            }
            if (!found) {
                reporter.reportError("The method: [" + methodName
                    + "] with the parameters and return value "
                    + " stipulated in the ddl could not be found.", lineNumber);
                return;
            }
        }

        protocolDescription.setMethods(expandedMethods);
        protocolDescription.setMethodsByClass(map);

        generate();
    }

    private boolean matchParameters(ParameterDeclaration parameters,
                                    CtClass[] types) {

        if (types.length == 0 && parameters.parameters.size() == 0) {
            return true;
        }

        if (parameters.parameters.size() != types.length) {
            return false;
        }

        if ("..".equals(parameters.parameters.getFirst().type.spelling)) {
            return true;
        }

        for (int i = 0; i < types.length; i++) {
            String type = parameters.parameters.get(i).type.spelling;

            if (!type.equals(types[i].getSimpleName())) {
                return false;
            }
        }

        return true;
    }

    private boolean matchMethodOrReturn(String re, String toMatch) {
        if (re.equals(toMatch)) {
            return true;
        }
        return "*".equals(re) || re.equals(toMatch);
        // return "*".equals(re) || Pattern.matches(re, toMatch);
    }

    /**
     * Generate protocol code
     */
    public abstract void generate();

    protected JavaMethod getRecovery(String recoveryName) {

        for (JavaMethod method : recovery) {
            if (method.getName().equals(recoveryName)) {
                return method;
            }
        }

        return null;
    }

    /**
     * Add packages to the classpool
     *
     * @param pool the classpool
     */
    protected final void addPackages(ClassPool pool) {
        for (String s : imports) {
            int i = s.lastIndexOf('*'); // wildcard import
            if (i > 0) {
                s = s.substring(0, i - 1);
            } else {
                s = s.substring(0, s.lastIndexOf('.'));
            }
            pool.importPackage(s);
        }
        try {
            pool.appendClassPath(clientOutputDirectory
                + System.getProperty("file.separator") + service);
        } catch (NotFoundException e) {
            e.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
        try {
            pool.appendClassPath(serverOutputDirectory
                + System.getProperty("file.separator") + service);
        } catch (NotFoundException e) {
            e.printStackTrace(); // To change body of catch statement use
            // File | Settings | File Templates.
        }
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    protected String makeParameterList(LinkedList<Parameter> parameters) {
        String parameterList = "";
        if (parameters != null) {
            for (Iterator<Parameter> it = parameters.listIterator(); it.hasNext(); ) {
                Parameter p = it.next();
                String n = p.type.spelling;
                if (p.referenceType != null) n = p.referenceType;
                String v = p.value.spelling;
                if ("".equals(parameterList)) {
                    parameterList += n + " " + v;
                } else {
                    parameterList += ", " + n + " " + v;
                }
            }
        }
        return parameterList;
    }

    protected String checkReturnType(MethodDescription md, CtMethod m,
                                     String methodName) {
        String returnType = "";
        // Check the return type implements Serializable
        try {
            returnType = m.getReturnType().getName();
            if (md.getReturnValue().type == Parameter.CALL_TYPE.REFERENCE) {
                return returnType;
            }
            CtClass serial = pool.get("java.io.Serializable");
            CtClass ext = pool.get("java.io.Externalizable");
            CtClass ct = pool.get(returnType);

            if (ct.isArray()) {
                ct = ct.getComponentType();
            }

            if (ct.isPrimitive()) {
                return returnType;
            }

            if (!ct.subtypeOf(serial) && !(ct.subtypeOf(ext))) {
                reporter.reportError("Error: Return class " + returnType
                    + " does not implement java.io.Serializable", lineNumber);
                return null;
            }
        } catch (NotFoundException e) {
            reporter.reportError(
                "Return class " + returnType + ", does not exist", lineNumber);
            return null;
        }
        return returnType;
    }

    protected String getInitializer(String type) {
        if ("byte".equals(type)) {
            return "0";
        }
        if ("short".equals(type)) {
            return "0";
        }
        if ("int".equals(type)) {
            return "0";
        }
        if ("long".equals(type)) {
            return "0";
        }
        if ("float".equals(type)) {
            return "0.0";
        }
        if ("double".equals(type)) {
            return "0.0";
        }
        if ("char".equals(type)) {
            return "0";
        }
        if ("boolean".equals(type)) {
            return "false";
        }
        return "";
    }

    protected boolean generateRecoveryRoutine(CtClass c, String recoveryName,
                                              String returnType) {

        // used by rmi protocol
        JavaMethod recoveryMethod = getRecovery(recoveryName);

        if (recoveryMethod == null) {
            reporter.reportError("recovery method: " + recoveryName
                + ", not found", lineNumber);
            return false;
        }

        String recoveryRoutine = "";
        try {
            String type = (recoveryMethod.getParameters().parameters.getFirst()).type.spelling;
            String value = (recoveryMethod.getParameters().parameters.getFirst()).value.spelling;
            CtClass exception = findClass(pool, type);
            if (exception == null) {
                reporter
                    .reportError(
                        "cannot find recovery parameter type: "
                            + type
                            + ", use fully qualified name or add to DDL import statement",
                        lineNumber);
                return false;
            }
            // check if we have added it already

            // recoveryRoutine = "private void _recoveryRoutine(" + type + " " +
            // value + ") {\n" +
            recoveryRoutine = "private void "
                + recoveryName
                + "("
                + type
                + " "
                + value
                + ") {\n"
                + "if (exceptionDepth > 1) {\n "
                + "   System.err.println(\"FATAL ERROR: Re-entering recovery routine after a previous failure\");\n "
                + "   System.err.println(\"Recovery is not possible\");\n "
                + "   System.err.println(\"Terminating application...\");\n "
                + "   System.exit(1);\n " + "}\n "
                + recoveryMethod.getJavaCode() + "}\n";
            Compiler.debug(recoveryRoutine);
            CtMethod mmm = CtMethod.make(recoveryRoutine, c);

            String s = mmm.getSignature();
            try {
                c.getMethod(recoveryName, s);
                return true; // already exists
            } catch (NotFoundException e) {

            }

            c.addMethod(mmm);
        } catch (CannotCompileException e) {
            reporter.reportError("cannot create recovery method: " + recoveryName,
                lineNumber);
            return false;
        }
        return true;
    }

    protected void generateRecoveryRoutine(String recoveryName,
                                           String returnType, CtMethod mmm) {

        JavaMethod recoveryMethod = getRecovery(recoveryName);

        if (recoveryMethod == null) {
            reporter.reportError("recovery method: " + recoveryName
                + ", not found", lineNumber);
        }

        try {
            String type = (recoveryMethod.getParameters().parameters.getFirst()).type.spelling;
            String value = (recoveryMethod.getParameters().parameters.getFirst()).value.spelling;
            CtClass exception = findClass(pool, type);
            if (exception == null) {
                reporter
                    .reportError(
                        "cannot find recovery parameter type: "
                            + type
                            + ", use fully qualified name or add to DDL import statement",
                        lineNumber);
                return;
            }
            if ("void".equals(returnType)) {
                mmm.addCatch(recoveryMethod.getJavaCode() + "return;", exception,
                    value);
            } else if ("byte".equals(returnType) || "short".equals(returnType)
                || "int".equals(returnType) || "long".equals(returnType)
                || "float".equals(returnType) || "double".equals(returnType)
                || "boolean".equals(returnType) || "char".equals(returnType)) {
                mmm.addCatch(recoveryMethod.getJavaCode() + returnType + " result="
                        + getInitializer(returnType) + ";return result;", exception,
                    value);
            } else {
                Compiler.debug(recoveryMethod.getJavaCode() + returnType
                    + " result=null;return result;");
                mmm.addCatch(recoveryMethod.getJavaCode() + returnType
                    + " result=null;return result;", exception, value);
            }
        } catch (CannotCompileException e) {
            reporter.reportError("cannot create recovery method: " + recoveryName,
                lineNumber);
        }

    }

    protected Parameter.CALL_TYPE[] getCallTypes(LinkedList<Parameter> parameters) {
        Parameter.CALL_TYPE[] types = new Parameter.CALL_TYPE[parameters.size()];
        int i = 0;
        for (Iterator<Parameter> it = parameters.listIterator(); it.hasNext(); ) {
            Parameter p = (Parameter) it.next();
            types[i] = p.callType;
        }
        return types;
    }

    protected String makeParameterValues(LinkedList<Parameter> parameters) {
        String values = "";
        for (Iterator<Parameter> it = parameters.listIterator(); it.hasNext(); ) {
            Parameter p = (Parameter) it.next();
            String v = p.value.spelling;
            if ("".equals(values)) {
                values += v + " ";
            } else {
                values += ", " + v;
            }
        }
        return values;
    }

    /**
     * Check parameters implement java.io.Serializable
     *
     * @param parameters a list of parameters
     */

    protected void checkParameters(LinkedList<Parameter> parameters) {
        for (Iterator<Parameter> it = parameters.listIterator(); it.hasNext(); ) {
            Parameter p = (Parameter) it.next();
            String n = p.type.spelling;

            CtClass cls = findClass(pool, n);
            if (cls.isArray()) {
                try {
                    cls = cls.getComponentType();
                } catch (NotFoundException e) {

                }
            }
            if (!cls.isPrimitive()) {
                try {
                    CtClass serial = pool.get("java.io.Serializable");
                    CtClass ext = pool.get("java.io.Externalizable");

                    if (!cls.subtypeOf(serial) && !(cls.subtypeOf(ext))) {
                        reporter.reportError("Error: parameter class " + n
                                + " does not implement java.io.Serializable",
                            lineNumber);
                        return;
                    }
                } catch (NotFoundException e) {
                    reporter.reportError("method " + n + ", does not exist",
                        lineNumber);
                }
            }
        }
    }

    protected String getRecoveryName(String methodName) {
        for (MethodDescription method : protocolDescription.getMethods()) {
            if (methodName.equals(method.getName())) {
                return method.getRecoveryName();
            }
        }
        // get default recovery
        for (MethodDescription method : protocolDescription.getMethods()) {
            if (methodName.equals("default")) {
                return method.getRecoveryName();
            }
        }
        return null; // should not happen !!
    }

    /**
     * Expands the parameters object to their fully qualified name
     *
     * @param parameters a LinkedList of parameters
     * @return the updated Parameter list
     * @throws NotFoundException
     */
    protected void expandParameters(LinkedList<Parameter> parameters)
        throws NotFoundException {

        for (int i = 0; i < parameters.size(); i++) {
            Parameter p = parameters.get(i);
            String type = p.type.spelling;
            if ("byte".equals(type) || "short".equals(type) || "int".equals(type)
                || "long".equals(type) || "char".equals(type)
                || "float".equals(type) || "double".equals(type)
                || "boolean".equals(type) || type.contains("[]")) {
                continue;
            }
            CtClass c = this.findClass(pool, p.type.spelling);
            if (c == null) {
                throw new NotFoundException("Class, " + p.type.spelling
                    + "not found");
            }
            p.type = new Identifier(c.getName(), new SourcePosition());
        }
    }

    protected LinkedList<Parameter> createParameters(CtMethod m)
        throws NotFoundException {
        LinkedList<Parameter> l = new LinkedList<Parameter>();
        Parameter p;
        CtClass[] c = m.getParameterTypes();
        for (int i = 0; i < c.length; i++) {
            p = new Parameter(Parameter.CALL_TYPE.COPY, new Identifier(c[i]
                .getName(), new SourcePosition()), new Identifier("a" + i,
                new SourcePosition()));
            l.add(p);
        }
        return l;
    }

    protected CtMethod getMethodByName(CtClass c, String name)
        throws NotFoundException {
        CtMethod allMethods[] = c.getMethods();
        for (CtMethod allMethod : allMethods) {
            if (allMethod.getName().equals(name)) {
                return allMethod;
            }
        }
        throw new NotFoundException("Method: " + name + " not found in class: "
            + c.getName());
    }

    protected CtMethod getMethod(CtClass c, MethodDescription md)
        throws NotFoundException {
        CtMethod allMethods[] = c.getMethods();
        for (CtMethod allMethod : allMethods) {
            if (!allMethod.getName().equals(md.getName())) {
                continue;
            }

            CtClass[] parameterTypes = allMethod.getParameterTypes();
            if (parameterTypes.length != md.getParameters().parameters.size()) {
                continue;
            }
            CtClass[] mdTypes = new CtClass[md.getParameters().parameters.size()];
            for (int i = 0; i < md.getParameters().parameters.size(); i++) {
                CtClass ct = findClass(pool,
                    md.getParameters().parameters.get(i).type.spelling);
                assert (ct != null);
                mdTypes[i] = ct;
            }
            boolean found = parametersEqual(parameterTypes, mdTypes);
            if (!found) {
                continue;
            }
            return allMethod;
        }
        throw new NotFoundException("Method: " + md.getName()
            + " not found in class: " + c.getName());
    }

    private boolean parametersEqual(CtClass[] p1, CtClass[] p2) {
        for (int i = 0; i < p1.length; i++) {
            if (!p1[i].equals(p2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a method or any code contained in the method is synchronized
     *
     * @param method the method to check
     * @return true if it uses any synchronized statements, false otherwise
     */
    public boolean checkSynchronized(CtMethod method) {
        boolean isSynchronized = false;
        if (Modifier.isSynchronized(method.getModifiers())) {
            reporter
                .reportWarning(
                    "method: "
                        + method.getName()
                        + ", is synchronized. This may have unintentional consequences.",
                    lineNumber);
            isSynchronized = true;
        }
        MethodInfo mi = method.getMethodInfo();
        CodeAttribute ca = mi.getCodeAttribute();
        CodeIterator ci = ca.iterator();
        while (ci.hasNext()) {
            int index;
            try {
                index = ci.next();
            } catch (BadBytecode badBytecode) {
                continue;
            }
            int op = ci.byteAt(index);
            if (op == Opcode.MONITORENTER || op == Opcode.MONITOREXIT) {
                reporter
                    .reportWarning(
                        "method: "
                            + method.getName()
                            + ", uses the synchronized operand. This may have unintentional consequences.",
                        lineNumber);
                isSynchronized = true;
                break;
            }
        }
        return isSynchronized;
    }

    @SuppressWarnings("unchecked")
    protected String setupTransferObject(CtMethod method, CtClass c,
                                         LinkedList<Parameter> parameters) {

        String methodBody = "  transfer.setCurrentHost(getCurrentHost());\n"
            + "  transfer.setMethod(\"" + method.getName() + "\");\n"
            + "  transfer.setClassName(\"" + c.getName() + "\");\n";
        return methodBody;
    }

    protected String setupTransferObjectParameters(CtMethod method, CtClass c,
                                                   LinkedList<Parameter> parameters) {

        String methodBody =
            "  Object[] o = new Object[" + parameters.size() + "];\n" +
                "  Class[]  p = new Class[" + parameters.size() + "];\n";

        for (int i = 0; i < parameters.size(); i++) {
            String par = parameters.get(i).value.spelling;
            String type = parameters.get(i).type.spelling;
            if ("byte".equals(type)) {
                methodBody += "  o[" + i + "] = new Byte(" + par + ");\n";
            } else if ("short".equals(type)) {
                methodBody += "  o[" + i + "] = new Short(" + par + ");\n";
            } else if ("int".equals(type)) {
                methodBody += "  o[" + i + "] = new Integer(" + par + ");\n";
            } else if ("long".equals(type)) {
                methodBody += "  o[" + i + "] = new Long(" + par + ");\n";
            } else if ("char".equals(type)) {
                methodBody += "  o[" + i + "] = new Character(" + par + ");\n";
            } else if ("float".equals(type)) {
                methodBody += "  o[" + i + "] = new Float(" + par + ");\n";
            } else if ("double".equals(type)) {
                methodBody += "  o[" + i + "] = new Double(" + par + ");\n";
            } else if ("boolean".equals(type)) {
                methodBody += "  o[" + i + "] = new Boolean(" + par + ");\n";
            } else {
                methodBody += "  o[" + i + "] = " + par + ";\n";
            }
        }
        for (int i = 0; i < parameters.size(); i++) {
            String type = parameters.get(i).type.spelling;
            String value = parameters.get(i).value.spelling;
            if ("byte".equals(type)) {
                methodBody += "  p[" + i + "] = Byte.TYPE;\n";
            } else if ("short".equals(type)) {
                methodBody += "  p[" + i + "] = Short.TYPE;\n";
            } else if ("int".equals(type)) {
                methodBody += "  p[" + i + "] = Integer.TYPE;\n";
            } else if ("long".equals(type)) {
                methodBody += "  p[" + i + "] = Long.TYPE;\n";
            } else if ("char".equals(type)) {
                methodBody += "  p[" + i + "] = Character.TYPE;\n";
            } else if ("float".equals(type)) {
                methodBody += "  p[" + i + "] = Float.TYPE;\n";
            } else if ("double".equals(type)) {
                methodBody += "  p[" + i + "] = Double.TYPE;\n";
            } else if ("boolean".equals(type)) {
                methodBody += "  p[" + i + "] = Boolean.TYPE;\n";
            } else if (type.contains("[]")) {
                methodBody += "  p[" + i + "] = " + value + ".getClass();\n";
            } else {
                try {
                    pool.get(type);
                    // Class.forName(type);
                } catch (NotFoundException e) {
                    reporter.reportError("cannot get Parameter type for class: "
                        + type, lineNumber);
                }
                methodBody += "  try {\n";
                methodBody += "     p[" + i + "] = Class.forName(\"" + type
                    + "\");\n";
                methodBody += "  } catch (ClassNotFoundException e) { \n";
                methodBody += "    e.printStackTrace(); \n";
                methodBody += "  }\n";
            }
        }
        methodBody += "  transfer.setParameters(o);\n"
            + "  transfer.setParameterTypes(p);\n";
        return methodBody;
    }

    public boolean hasNullConstructor(CtClass c) {
        CtConstructor[] cons = c.getConstructors();
        for (int i = 0; i < cons.length; i++) {
            if (cons[i].isConstructor()) {
                return true;
            }
        }
        return false;
    }

    protected void alterToImplementInterface(CtClass c, CtClass inf) {
        try {
            CtClass ifaces[] = c.getInterfaces();
            for (CtClass cls : ifaces) {
                if (cls.getName().equals(inf.getName())) {
                    return;
                }
            }

            CtClass m1[] = new CtClass[ifaces.length + 1];
            System.arraycopy(ifaces, 0, m1, 0, ifaces.length);
            m1[ifaces.length] = inf;
            c.setInterfaces(m1);
            c.defrost();
        } catch (NotFoundException e) {
            reporter.reportError("cannot alter class to implement interface: "
                + e.getMessage(), lineNumber);
        }
    }
}
