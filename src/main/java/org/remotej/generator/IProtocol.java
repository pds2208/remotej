package org.remotej.generator;

import org.remotej.ddl.ErrorHandler;
import org.remotej.ddl.analyser.SourcePosition;

import java.util.Vector;

/**
 * IProtocol
 * <p/>
 * Version 1.0
 * <p/>
 * Copyright Data Systems & Solutions (2007)
 * This source is the property of Data Systems & Solutions Ltd and
 * must not be used for any purpose by any party unless specific
 * written consent has been given by an authorised signatory of Data
 * Systems and Solutions Ltd.
 */
public interface IProtocol {

   void validateOptions() throws OptionException;

   void setOptions(ProtocolOptions opt);

   void setServerOutputDirectory(String serverOutputDirectory);

   void setClientOutputDirectory(String clientOutputDirectory);

   void setService(String spelling);

   void setImports(Vector<String> imports);

   void setReporter(ErrorHandler reporter);

   void setLineNumber(SourcePosition position);

   void setRecovery(Vector<JavaMethod> recovery);

   void setProtocolDescription(ProtocolDescription protocols);

   void generateAll();
}
