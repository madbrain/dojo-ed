package com.github.madbrain.dojo.ed.parse;

public class TokenStream {
    private String content;
    private int index = 0;

    public TokenStream(String content) {
        this.content = content;
    }

    public Token nextToken() {
        while (true) {
            if (index >= content.length()) {
                return new Token(TokenKind.EOF, null);
            }
            char c = content.charAt(index++);
            if (isDigit(c)) {
                return digit(c);
            }
            if (c == '/') {
                return search();
            }
            if (c == ',') {
                return new Token(TokenKind.COMA, null);
            }
            if (c == '+') {
                return new Token(TokenKind.ADD, null);
            }
            if (c == '-') {
                return new Token(TokenKind.SUB, null);
            }
            if (c == '$') {
                return new Token(TokenKind.DOLLAR, null);
            }
            if (c == '.') {
                return new Token(TokenKind.DOT, null);
            }
            return new Token(TokenKind.COMMAND, content.substring(index-1));
        }
    }

    private Token search() {
        StringBuilder buffer = new StringBuilder();
        while (true) {
            if (index >= content.length()) {
                break;
            }
            char c = content.charAt(index++);
            if (c == '/') {
                break;
            }
            buffer.append(c);
        }
        return new Token(TokenKind.SEARCH, buffer.toString());
    }

    private Token digit(char c) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(c);
        while (true) {
            if (index >= content.length()) {
                break;
            }
            c = content.charAt(index++);
            if (!isDigit(c)) {
                --index;
                break;
            }
            buffer.append(c);
        }
        return new Token(TokenKind.INT, buffer.toString());
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
