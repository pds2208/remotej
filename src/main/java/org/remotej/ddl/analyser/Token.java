package org.remotej.ddl.analyser;

public final class Token {

    int kind;
    final String spelling;
    final SourcePosition position;

    public Token(int kind, String spelling, SourcePosition position) {

        this.kind = kind;
        this.spelling = spelling;

        if (kind == Token.IDENTIFIER) {
            for (int k = SERVICE; k <= RESTORE; k++) {
                if (spelling.equals(tokenTable[k])) {
                    this.kind = k;
                    break;
                }
            }
        }

        this.position = position;
    }

    public static String spell(int kind) {
        return tokenTable[kind];
    }

    public final String toString() {
        return "Kind=" + kind + ", spelling=" + spelling +
            ", position=" + position;
    }

    // Token classes...

    // literals, identifiers, operators...
    public static final int INTLITERAL = 0;
    public static final int CHARLITERAL = 1;
    public static final int IDENTIFIER = 2;

    // reserved words
    public static final int SERVICE = 3;
    public static final int PROTOCOL = 4;
    public static final int POINTCUT = 5;
    public static final int IMPORT = 6;

    public static final int RECOVERY = 7;
    public static final int OPTIONS = 8;
    public static final int DOTDOT = 9;
    public static final int REF = 10;
    public static final int COPY = 11;
    public static final int RESTORE = 12;

    public static final int DOT = 10;
    public static final int EQUALS = 11;
    public static final int SEMICOLON = 12;
    public static final int COMMA = 13;
    public static final int COLON = 14;

    // brackets...
    public static final int LPAREN = 15;
    public static final int RPAREN = 16;
    public static final int LCURLY = 17;
    public static final int RCURLY = 18;
    public static final int QUOTE = 19;
    public static final int LBRACKET = 20;
    public static final int RBRACKET = 21;

    // special tokens...
    public static final int EOT = 22;
    public static final int ERROR = 23;

    private static final String[] tokenTable = new String[]{
        "<int>", "<char>", "<identifier>", "service", "protocol",
        "pointcut", "import", "recovery",
        "options", "..", "ref", "copy", "restore",
        ".", "=", ";", ",", ":", "(",
        ")", "{", "}", "\"", "[", "]", "", "<error>"
    };
}
