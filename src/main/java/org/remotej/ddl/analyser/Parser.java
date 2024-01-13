package org.remotej.ddl.analyser;

import org.remotej.ddl.ErrorHandler;
import org.remotej.ddl.trees.ClassValue;
import org.remotej.ddl.trees.*;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;


public final class Parser {

    private final Scanner lexicalAnalyser;
    private final ErrorHandler errorHandler;
    private Token currentToken;
    private SourcePosition previousTokenPosition;

    public Parser(Scanner lexer, ErrorHandler reporter) {
        lexicalAnalyser = lexer;
        errorHandler = reporter;
        previousTokenPosition = new SourcePosition();
    }

// accept checks whether the current token matches tokenExpected.
// If so, fetches the next token.
// If not, reports a syntactic error.

    private void accept(int tokenExpected) throws SyntaxError {
        if (currentToken.kind == tokenExpected) {
            previousTokenPosition = currentToken.position;
            currentToken = lexicalAnalyser.scan();
        } else {
            syntaxError(Token.spell(tokenExpected) + " expected here");
        }
    }

    private void acceptIt() {
        previousTokenPosition = currentToken.position;
        currentToken = lexicalAnalyser.scan();
    }

// start records the position of the start of a phrase.
// This is defined to be the position of the first
// character of the first token of the phrase.

    private void start(SourcePosition position) {
        position.setStart(currentToken.position.getStart());
    }

// finish records the position of the end of a phrase.
// This is defined to be the position of the last
// character of the last token of the phrase.

    private void finish(SourcePosition position) {
        position.setEnd(previousTokenPosition.getEnd());
    }

    private void syntaxError(String messageTemplate) throws SyntaxError {
        SourcePosition pos = currentToken.position;
        errorHandler.reportError(messageTemplate, pos);
        throw new SyntaxError();
    }

///////////////////////////////////////////////////////////////////////////////
//
// SERVICE
//
///////////////////////////////////////////////////////////////////////////////

    public final Service parseService() {

        previousTokenPosition.setStart(0);
        previousTokenPosition.setEnd(0);
        currentToken = lexicalAnalyser.scan();
        Service service;
        ClassImportStatement is = null;

        try {
            if (currentToken.kind == Token.IMPORT) {
                SourcePosition pos = new SourcePosition();
                start(pos);
                is = parseSingleClassImportStatement();
                accept(Token.SEMICOLON);
                while (currentToken.kind == Token.IMPORT) {
                    ClassImportStatement ci = parseSingleClassImportStatement();
                    is = new SequentialClassImportStatement(is, ci, pos);
                    accept(Token.SEMICOLON);
                    finish(pos);
                }
            }

            accept(Token.SERVICE);
            Identifier svc = parseIdentifier();
            accept(Token.LCURLY);

            Statement s = parseStatement();
            accept(Token.RCURLY);

            if (currentToken.kind != Token.EOT) {
                syntaxError("\"%\" not expected after end of service decleration"
                );
            }
            service = new Service(svc, is, s, previousTokenPosition);
        } catch (SyntaxError s) {
            System.err.println("Syntax error");
            return null;
        }
        return service;
    }

    private ClassImportStatement parseSingleClassImportStatement() throws SyntaxError {
        SourcePosition pos = new SourcePosition();
        ClassImportStatement o;

        acceptIt();
        start(pos);
        String s = scanJavaClassName();
        o = new ClassImportStatement(new Identifier(s, pos), pos);
        return o;
    }

    private Option parseOptions() throws SyntaxError {
        SourcePosition pos = new SourcePosition();
        Option o;

        acceptIt();
        start(pos);
        accept(Token.LCURLY);

        Identifier name = new Identifier(currentToken.spelling, pos);
        acceptIt();

        accept(Token.EQUALS);

        o = parseSingleOption(name);
        accept(Token.SEMICOLON);

        while (currentToken.kind == Token.IDENTIFIER) {
            name = new Identifier(currentToken.spelling, pos);
            acceptIt();
            accept(Token.EQUALS);
            Option opt = parseSingleOption(name);
            o = new SequentialOption(o, opt, pos);
            accept(Token.SEMICOLON);
            finish(pos);
        }
        accept(Token.RCURLY);
        return o;
    }

    private Option parseSingleOption(Identifier name) throws SyntaxError {
        SourcePosition pos = new SourcePosition();
        Option opt;
        start(pos);

        Identifier value = new Identifier(currentToken.spelling, pos);
        acceptIt();

        opt = new NameValueOption(name, value, pos);
        finish(pos);
        return opt;
    }

    private Statement parseStatement() throws SyntaxError {
        SourcePosition pos = new SourcePosition();
        start(pos);
        Statement st = parseSingleStatement();

        while (currentToken.kind == Token.RECOVERY ||
            currentToken.kind == Token.PROTOCOL) {
            Statement s = parseSingleStatement();
            st = new SequentialStatement(st, s, pos);
        }
        return st;
    }

    private Statement parseSingleStatement() throws SyntaxError {
        SourcePosition pos = new SourcePosition();
        Statement st = null;

        start(pos);

        switch (currentToken.kind) {
            case Token.RECOVERY:
                st = parseRecovery();
                finish(pos);
                break;
            case Token.PROTOCOL:
                st = parseProtocol();
                finish(pos);
                break;
            default:
                syntaxError("Invalid statement");
        }
        return st;
    }

    private Statement parseProtocol() throws SyntaxError {
        SourcePosition pos = new SourcePosition();

        start(pos);
        accept(Token.PROTOCOL);
        accept(Token.COLON);

        // we accept any protocol string and use it to load
        // the protocol implementation class
        // using the convention upper(protocol) + Protocol

        String protocol = currentToken.spelling;

        acceptIt();

        accept(Token.LCURLY);
        ProtocolStatement ps = new ProtocolStatement(protocol, pos);

        Statement st = new SequentialStatement(ps, parseSingleProtocolStatement(), pos);

        while (currentToken.kind == Token.OPTIONS ||
            currentToken.kind == Token.POINTCUT) {
            Statement s = parseSingleProtocolStatement();
            st = new SequentialStatement(st, s, pos);
        }

        accept(Token.RCURLY);

        return st;
    }

    private Statement parseSingleProtocolStatement() throws SyntaxError {
        SourcePosition pos = new SourcePosition();
        Statement st = null;

        start(pos);

        switch (currentToken.kind) {
            case Token.OPTIONS:
                st = parseOptions();
                finish(pos);
                break;
            case Token.POINTCUT:
                st = parsePointcut();
                finish(pos);
                break;
            default:
                syntaxError("Invalid statement");
        }
        return st;
    }

    private RecoveryStatement parseRecovery() throws SyntaxError {
        SourcePosition pos = new SourcePosition();

        start(pos);
        accept(Token.RECOVERY);
        Identifier id = new Identifier(scanJavaClassName(), pos);
        //MethodName methodName = new MethodName(new Identifier(scanJavaClassName(), pos), pos);
        ParameterDeclaration parameters = parseParameters();
        String recoveryCode = parseEmbeddedCode();
        accept(Token.RCURLY);
        return new RecoveryStatement(id, parameters, new Identifier(recoveryCode, pos), pos);
    }

    private String parseEmbeddedCode() throws SyntaxError {
        start(new SourcePosition());

        String javaCode = lexicalAnalyser.scanJavaCode(true);
        currentToken = lexicalAnalyser.scan();
        return javaCode;
    }

    private PointcutStatement parsePointcut() throws SyntaxError {
        SourcePosition pos = new SourcePosition();

        start(pos);
        acceptIt(); // pointcut

        // default call type;
        Parameter.CALL_TYPE type = Parameter.CALL_TYPE.COPY;

        if (currentToken.kind == Token.COPY) {
            acceptIt();
            type = Parameter.CALL_TYPE.COPY;
        } else if (currentToken.kind == Token.RESTORE) {
            acceptIt();
            type = Parameter.CALL_TYPE.RESTORE;
        } else if (currentToken.kind == Token.REF) {
            acceptIt();
            type = Parameter.CALL_TYPE.REFERENCE;
        }

        String ret = scanJavaClassName();

        while (currentToken.kind == Token.LBRACKET) {
            acceptIt();
            if (currentToken.kind != Token.RBRACKET) {
                syntaxError("Expecting ] here.");
            }
            acceptIt();
            ret += "[]";
        }

        ReturnValue returnValue =
            new ReturnValue(new Identifier(ret, pos), type, pos);

        Identifier s = new Identifier(scanJavaClassName(), pos);
        StringTokenizer st = new StringTokenizer(s.spelling, ".");

        String cls, met;
        try {
            cls = st.nextToken();
            met = st.nextToken();
        } catch (NoSuchElementException ne) {
            cls = null;
            met = null;
        }

        if (cls == null || met == null) {
            syntaxError("ClassName.MethodName Expected");
        }

        Identifier cl = new Identifier(cls, pos);
        Identifier m1 = new Identifier(met, pos);
        MethodName methodName = new MethodName(m1, pos);
        ParameterDeclaration parameters = parseParameters();

        ClassValue aClass = new ClassValue(cl, returnValue, methodName, parameters, pos);

        PointcutDeclaration es = new PointcutDeclaration(aClass, pos);

        accept(Token.LCURLY);

        if (currentToken.kind == Token.RECOVERY) {
            acceptIt(); // recovery
            accept(Token.EQUALS); // =
            Identifier id = parseIdentifier(); // recovery name | continue | abort | nextServer
            es.recovery = new RecoveryOption(id, pos);
            accept(Token.SEMICOLON);
        }
        accept(Token.RCURLY);
        finish(pos);
        return es;
    }

    private String scanJavaClassName() {
        SourcePosition pos = new SourcePosition();

        start(pos);

        String className = "";

        className += parseJavaIdentifier().spelling;

        while (currentToken.kind == Token.DOT) {
            acceptIt();
            className += "." + parseJavaIdentifier().spelling;
        }
        finish(pos);
        return className;
    }

    private ParameterDeclaration parseParameters() throws SyntaxError {
        SourcePosition pos = new SourcePosition();
        ParameterDeclaration parameters = new ParameterDeclaration(pos);

        start(pos);

        accept(Token.LPAREN);
        // no parameters
        if (currentToken.kind == Token.RPAREN) {
            acceptIt();
            return parameters;
        }

        // Default call type
        Parameter.CALL_TYPE callType = Parameter.CALL_TYPE.COPY;

        if (currentToken.kind == Token.COPY) {
            acceptIt();
            callType = Parameter.CALL_TYPE.COPY;
        } else if (currentToken.kind == Token.RESTORE) {
            acceptIt();
            callType = Parameter.CALL_TYPE.RESTORE;
        } else if (currentToken.kind == Token.REF) {
            acceptIt();
            callType = Parameter.CALL_TYPE.REFERENCE;
        }

        if (currentToken.kind == Token.DOTDOT) {
            acceptIt();
            parameters.add(callType, new Identifier("*", pos), new Identifier("undefined", pos));
            accept(Token.RPAREN);
            finish(pos);
            return parameters;
        }

        String typ = scanJavaClassName();

        // check for an array
        if (currentToken.kind == Token.LBRACKET) {
            acceptIt();
            if (currentToken.kind != Token.RBRACKET) {
                throw new SyntaxError("] expected");
            }
            acceptIt();
            typ += "[]";
        }

        String val;
        if (currentToken.kind == Token.COMMA || currentToken.kind == Token.RPAREN) {
            val = "undefined";
        } else {
            val = parseIdentifier().spelling;
        }

        // Arrays, could be multi-dimentional
        while (currentToken.kind == Token.LBRACKET) {
            acceptIt();
            if (currentToken.kind != Token.RBRACKET) {
                throw new SyntaxError("] expected");
            }
            acceptIt();
            typ += "[]";
        }

        Identifier type = new Identifier(typ, pos);
        Identifier value = new Identifier(val, pos);

        parameters.add(callType, type, value);

        while (currentToken.kind == Token.COMMA) {
            acceptIt();
            type = new Identifier(scanJavaClassName(), pos);
            if (currentToken.kind == Token.COMMA || currentToken.kind == Token.RPAREN) {
                value = new Identifier("undefined", pos);
            } else {
                value = parseIdentifier();
            }
            parameters.add(callType, type, value);
        }
        accept(Token.RPAREN);
        finish(pos);
        return parameters;
    }

///////////////////////////////////////////////////////////////////////////////
//
// LITERALS
//
///////////////////////////////////////////////////////////////////////////////

// parseIntegerLiteral parses an integer-literal, and constructs
// a leaf AST to represent it.

    /**
     * Read next identifier regardless of whether its an identifier or not. Used for import statement
     *
     * @return the identifier
     */
    private Identifier parseJavaIdentifier() {
        Identifier I;

        previousTokenPosition = currentToken.position;
        String spelling = currentToken.spelling;
        I = new Identifier(spelling, previousTokenPosition);
        currentToken = lexicalAnalyser.scan();
        return I;
    }

// parseIdentifier parses an identifier

    private Identifier parseIdentifier() throws SyntaxError {
        Identifier I;

        if (currentToken.kind == Token.IDENTIFIER) {
            previousTokenPosition = currentToken.position;
            String spelling = currentToken.spelling;
            I = new Identifier(spelling, previousTokenPosition);
            currentToken = lexicalAnalyser.scan();
        } else {
            I = null;
            syntaxError("identifier expected");
        }
        return I;
    }

}
