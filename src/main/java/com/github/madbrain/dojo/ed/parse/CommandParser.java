package com.github.madbrain.dojo.ed.parse;

import com.github.madbrain.dojo.ed.Command;
import com.github.madbrain.dojo.ed.Interval;

public class CommandParser {
    private final TokenStream stream;
    private final BufferAccess bufferAccess;
    private Token token;

    public CommandParser(TokenStream stream, BufferAccess bufferAccess) {
        this.stream = stream;
        this.bufferAccess = bufferAccess;
    }

    public Command parse() {
        scanToken();
        if (token.kind() != TokenKind.COMMAND) {
            Interval interval = parseInterval();
            String command = token.kind() == TokenKind.COMMAND ? token.value() : "";
            return new Command(interval, command);
        } else {
            return new Command(new Interval(bufferAccess.currentLine(), bufferAccess.currentLine()), token.value());
        }
    }

    private Interval parseInterval() {
        int start = parseExpression();
        if (test(TokenKind.COMA)) {
            int end = parseExpression();
            return new Interval(start, end);
        }
        return new Interval(start, start);
    }

    private boolean test(TokenKind kind) {
        if (token.kind() != kind) {
            return false;
        }
        scanToken();
        return true;
    }

    private int parseExpression() {
        int value = parseAtom();
        while (true) {
            if (token.kind() == TokenKind.ADD) {
                scanToken();
                value += parseAtom();
            } else if (token.kind() == TokenKind.SUB) {
                scanToken();
                value -= parseAtom();
            } else {
                break;
            }
        }
        return value;
    }

    private int parseAtom() {
        if (token.kind() == TokenKind.INT) {
            int value = Integer.parseInt(token.value());
            scanToken();
            return value;
        }
        if (token.kind() == TokenKind.DOLLAR) {
            scanToken();
            return bufferAccess.lastLineIndex();
        }
        if (token.kind() == TokenKind.DOT) {
            scanToken();
            return bufferAccess.currentLine();
        }
        if (token.kind() == TokenKind.SEARCH) {
            int value = bufferAccess.searchLine(token.value());
            if (value == 0) {
                throw new RuntimeException("not found");
            }
            scanToken();
            return value;
        }
        throw new RuntimeException("EXPR " + token);
    }

    private void scanToken() {
        this.token = stream.nextToken();
    }
}
