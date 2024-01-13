package org.remotej.generator;

import org.remotej.ddl.ErrorHandler;
import org.remotej.ddl.analyser.SourcePosition;

import java.util.Vector;

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
