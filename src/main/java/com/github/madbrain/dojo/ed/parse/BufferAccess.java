package com.github.madbrain.dojo.ed.parse;

public interface BufferAccess {
    int currentLine();

    int lastLineIndex();

    int searchLine(String content);
}
