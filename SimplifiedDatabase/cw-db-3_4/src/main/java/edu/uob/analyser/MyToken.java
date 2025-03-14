package edu.uob.analyser;

public class MyToken {
    private TokenType type;
    private String value;

    public MyToken(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return type + ": \"" + value + "\"";
    }
}
