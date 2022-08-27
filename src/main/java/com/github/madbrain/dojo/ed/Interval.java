package com.github.madbrain.dojo.ed;

import com.github.madbrain.dojo.ed.parse.BufferAccess;

public record Interval(int start, int end) {
    public boolean check(BufferAccess bufferAccess) {
        return start >= 1
                && start <= bufferAccess.lastLineIndex()
                && end >= 1
                && end <= bufferAccess.lastLineIndex()
                && end >= start;
    }
}
