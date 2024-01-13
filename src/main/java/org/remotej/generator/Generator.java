package org.remotej.generator;

import org.remotej.ddl.ErrorHandler;
import org.remotej.ddl.analyser.SourcePosition;
import org.remotej.ddl.trees.ClassValue;
import org.remotej.ddl.trees.*;

import java.util.HashSet;
import java.util.Vector;

/**
 * Copyright(c) Paul Soule. All rights reserved. <p/> This file is part of the
 * RemoteJ system. <p/> Created at 2:29:12 PM on Mar 26, 2006
 */
public final class Generator implements Visitor {

    private final ErrorHandler reporter;
    private final ServiceDescription serviceDescription;
    // the list of pointcuts
    private final Vector<ProtocolDescription> protocolDescriptionList;

    private final Vector<String> imports = new Vector<String>();
    private String serverOutputDirectory;
    private String clientOutputDirectory;
    private ProtocolDescription protocolDescription = null;

    public Generator(ErrorHandler reporter) {
        this.reporter = reporter;
        serviceDescription = new ServiceDescription();
        protocolDescriptionList = new Vector<ProtocolDescription>();
        serviceDescription.setProtocols(protocolDescriptionList);
    }

    public String getServerOutputDirectory() {
        return serverOutputDirectory;
    }

    public void setServerOutputDirectory(String outputDirectory) {
        this.serverOutputDirectory = outputDirectory;
    }

    public String getClientOutputDirectory() {
        return clientOutputDirectory;
    }

    public void setClientOutputDirectory(String outputDirectory) {
        this.clientOutputDirectory = outputDirectory;
    }

    public void generate(Service ast) {
        ast.visit(this, null);

        if (reporter.getNumErrors() > 0) {
            return;
        }

        // check for duplicate methods
        HashSet<MethodDescription> methods = new HashSet<MethodDescription>();
        for (ProtocolDescription protocols : protocolDescriptionList) {
            Vector<MethodDescription> v = protocols.getMethods();
            for (MethodDescription names : v) {
                for (MethodDescription m : methods) {
                    if (m.getClassName().equals(names.getClassName())
                        && m.getName().equals(names.getName())
                        && parametersEqual(m.getParameters(), names
                        .getParameters())
                        && m.getReturnValue()
                        .equals(names.getReturnValue())) {
                        reporter.reportError("The method '"
                                + names.getReturnValue() + " "
                                + names.getClassName() + "." + names.getName()
                                + "' has already been declared for a protocol",
                            ast.position);
                        return;
                    }
                }
                methods.add(names);
            }
        }

        // generate pointcuts
        for (ProtocolDescription protocols : protocolDescriptionList) {

            ProtocolOptions opt = protocols.getProtocolOptions();
            String p = opt.getOptionValue(ProtocolOptions.PROTOCOL);

            IProtocol protocol = null;
            try {
                protocol = ProtocolFactory.getProtocol(p);
            } catch (ProtocolFactoryException e) {
                reporter.reportError(e.getMessage(), new SourcePosition(0, 0));
            }

            protocol.setOptions(opt);
            protocol.setServerOutputDirectory(serverOutputDirectory);
            protocol.setClientOutputDirectory(clientOutputDirectory);
            protocol.setService(ast.serviceName.spelling);
            protocol.setImports(imports);
            protocol.setReporter(reporter);
            protocol.setLineNumber(ast.position);

            protocol.setRecovery(serviceDescription.getRecovery());
            protocol.setProtocolDescription(protocols);

            try {
                protocol.validateOptions();
            } catch (OptionException e) {
                reporter.reportError(e.getMessage(), new SourcePosition(e
                    .getLineNumber(), e.getLineNumber()));
                return;
            }

            protocol.generateAll();
        }
    }

    private boolean parametersEqual(ParameterDeclaration parameters,
                                    ParameterDeclaration parameters2) {
        if (parameters.parameters.size() != parameters2.parameters.size()) {
            return false;
        }
        for (int i = 0; i < parameters.parameters.size(); i++) {
            String type = parameters.parameters.get(i).type.spelling;
            if (!type.equals(parameters2.parameters.get(i).type.spelling)) {
                return false;
            }
        }
        return true;
    }

    private String getBaseClassName(String s) {
        return s.substring(s.lastIndexOf('.') + 1, s.length());
    }

    public Object visitNameValueOption(NameValueOption ast, Object o) {
        ProtocolOptions option = protocolDescription.getProtocolOptions();
        try {
            option.addOption(ast.name.spelling, ast.value.spelling,
                ast.position.getStart());
        } catch (DuplicateOptionException e) {
            reporter.reportError("Error: " + e.getMessage(), ast.position);
        }
        return null;
    }

    public  Object visitSequentialClassImportStatement(
        SequentialClassImportStatement ast, Object o) {
        ast.st1.visit(this, null);
        ast.st2.visit(this, null);
        return null;
    }

    public  Object visitClassImportStatement(ClassImportStatement ast,
                                                  Object o) {
        imports.add(ast.importStatement.spelling);
        return null;
    }

    public  Object visitMethod(Methods ast, Object o) {
        ast.methodName.visit(this, null);
        ast.parameters.visit(this, null);
        ast.returnValue.visit(this, null);
        MethodDescription method = new MethodDescription();
        method.setName(ast.methodName.methodValue.spelling);
        method.setReturnValue(ast.returnValue);
        method.setParameters(ast.parameters);

        protocolDescription.addMethod(method);
        // Could be that there is no options set
        if (ast.option != null) {
            ast.option.visit(this, null);
        }

        return null;
    }

    public  Object visitMethodName(MethodName ast, Object o) {
        ast.methodValue.visit(this, null);
        return null;
    }

    public Object visitSequentialMethods(SequentialMethods ast, Object o) {
        ast.opt1.visit(this, null);
        ast.opt2.visit(this, null);
        return null;
    }

    public Object visitPointcutMethods(PointcutMethods ast, Object o) {
        ast.methods.visit(this, null);
        return null;
    }

    public Object visitImportMethods(ImportMethods ast, Object o) {
        ast.methods.visit(this, null);
        return null;
    }

    public Object visitReturnValue(ReturnValue ast, Object o) {
        ast.returnValue.visit(this, null);
        return null;
    }

    public Object visitClassValue(ClassValue ast, Object o) {
        ast.returnValue.visit(this, null);
        ast.methodName.visit(this, null);
        ast.parameters.visit(this, null);
        // generate method names
        MethodDescription method = new MethodDescription();
        method.setName(ast.methodName.methodValue.spelling);
        method.setReturnValue(ast.returnValue);
        method.setParameters(ast.parameters);
        method.setClassName(ast.className.spelling);
        RecoveryOption rn = ((PointcutDeclaration) o).recovery;
        if (null == rn) {
            method.setRecoveryName("abort");
        } else {
            method
                .setRecoveryName(((PointcutDeclaration) o).recovery.recoveryName.spelling);
        }
        protocolDescription.addMethod(method);
        return null;
    }

    public Object visitImportDeclaration(ImportDeclaration ast, Object o) {
        ast.aClass.visit(this, null);
        return null;
    }

    public Object visitPointcutDeclaration(PointcutDeclaration ast, Object o) {
        ast.aClass.visit(this, ast);
        return null;
    }

    public Object visitService(Service ast, Object o) {
        ast.imports.visit(this, null);
        ast.service.visit(this, null);
        return null;
    }

    public Object visitSequentialOption(SequentialOption ast, Object o) {
        ast.opt1.visit(this, o);
        ast.opt2.visit(this, o);
        return null;
    }

    public Object visitSequentialStatement(SequentialStatement ast, Object o) {
        ast.smt1.visit(this, null);
        ast.smt2.visit(this, null);
        return null;
    }

    public Object visitSequentialImportStatement(SequentialImportStatement ast,
                                                 Object o) {
        ast.st1.visit(this, null);
        ast.st2.visit(this, null);
        return null;
    }

    public Object visitSequentialPointcutStatement(
        SequentialPointcutStatement ast, Object o) {
        ast.st1.visit(this, null);
        ast.st2.visit(this, null);
        return null;
    }

    public Object visitImportOptions(ImportOptions ast, Object o) {
        ast.options.visit(this, null);
        return null;
    }

    public Object visitPointcutOptions(PointcutOptions ast, Object o) {
        ast.options.visit(this, null);
        return null;
    }

    public Object visitRecoveryStatement(RecoveryStatement ast, Object o) {
        if (ast.parameters.parameters.isEmpty()) {
            reporter.reportError("recovery parameters have not been declared",
                ast.position);
        }
        serviceDescription.getRecovery().add(
            new JavaMethod(ast.recoveryName.spelling, ast.parameters,
                ast.recoveryCode.spelling));
        return null;
    }

    public Object visitIdentifier(Identifier ast, Object o) {
        return null;
    }

    public Object visitIntegerLiteral(IntegerLiteral ast, Object o) {
        return null;
    }

    public Object visitParameterDeclaration(ParameterDeclaration ast, Object o) {
        return null;
    }

    public Object visitRecoveryOption(RecoveryOption ast, Object o) {
        MethodDescription methodDescription = protocolDescription
            .getcurrentMethod();
        if (methodDescription.getRecoveryName() != null) {
            reporter.reportError("recovery option has already been declared",
                ast.position);
        }

        if ("nextServer".equals(ast.recoveryName.spelling)) {
            methodDescription.setRecoveryName(ast.recoveryName.spelling);
            return null;
        }
        if ("continue".equals(ast.recoveryName.spelling)) {
            methodDescription.setRecoveryName(ast.recoveryName.spelling);
            return null;
        }
        if ("abort".equals(ast.recoveryName.spelling)) {
            methodDescription.setRecoveryName(ast.recoveryName.spelling);
            return null;
        }

        Vector<JavaMethod> recovery = serviceDescription.getRecovery();
        JavaMethod rec = findJavaMethod(ast.recoveryName.spelling, recovery);
        if (rec == null) {
            reporter.reportError("The recovery method: "
                    + ast.recoveryName.spelling + " does not exist",
                ast.position);
        }
        methodDescription.setRecoveryName(ast.recoveryName.spelling);
        return null;
    }

    public Object visitBooleanValue(BooleanValue ast, Object o) {
        return null;
    }

    public Object visitProtocolStatement(ProtocolStatement ast, Object o) {
        protocolDescription = new ProtocolDescription();
        protocolDescription.setProtocol(ast.protocol);
        protocolDescriptionList.add(protocolDescription);

        ProtocolOptions opt = protocolDescription.getProtocolOptions();
        try {
            opt.addOption(ProtocolOptions.PROTOCOL, ast.protocol, ast.position
                .getStart());
        } catch (DuplicateOptionException e) {
            reporter.reportError("protocol option has already been declared",
                ast.position);
        }

        return null;
    }

    private JavaMethod findJavaMethod(String name, Vector<JavaMethod> list) {
        for (JavaMethod l : list) {
            if (l.getName().equals(name)) {
                return l;
            }
        }
        return null;
    }
}
