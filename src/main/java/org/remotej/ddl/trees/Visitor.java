package org.remotej.ddl.trees;

/**
 * Copyright (c) 2006 Paul Soule
 * School of Computing, University of Glamorgan, Pontypridd, Wales UK CF37 1DL.
 * <p/>
 * All rights reserved.
 * <p/>
 * File Created at 12:18:14 on 31-Jan-2006
 */

public interface Visitor {

   Object visitNameValueOption(NameValueOption aThis, Object o);

   Object visitSequentialClassImportStatement(SequentialClassImportStatement ast, Object o);

   Object visitClassImportStatement(ClassImportStatement ast, Object o);

   Object visitMethod(Methods ast, Object o);

   Object visitMethodName(MethodName ast, Object o);

   Object visitSequentialMethods(SequentialMethods ast, Object o);

   Object visitPointcutMethods(PointcutMethods ast, Object o);

   Object visitImportMethods(ImportMethods ast, Object o);

   Object visitReturnValue(ReturnValue ast, Object o);

   Object visitClassValue(ClassValue ast, Object o);

   Object visitImportDeclaration(ImportDeclaration ast, Object o);

   Object visitPointcutDeclaration(PointcutDeclaration ast, Object o);

   Object visitService(Service ast, Object o);

   Object visitSequentialOption(SequentialOption ast, Object o);

   Object visitSequentialStatement(SequentialStatement ast, Object o);

   Object visitSequentialImportStatement(SequentialImportStatement ast, Object o);

   Object visitSequentialPointcutStatement(SequentialPointcutStatement ast, Object o);

   Object visitImportOptions(ImportOptions ast, Object o);

   Object visitPointcutOptions(PointcutOptions ast, Object o);

   Object visitRecoveryStatement(RecoveryStatement ast, Object o);

   Object visitIdentifier(Identifier ast, Object o);

   Object visitIntegerLiteral(IntegerLiteral ast, Object o);

   Object visitParameterDeclaration(ParameterDeclaration ast, Object o);

   Object visitRecoveryOption(RecoveryOption ast, Object o);

   Object visitBooleanValue(BooleanValue ast, Object o);

   Object visitProtocolStatement(ProtocolStatement protocolStatement, Object o);

}
