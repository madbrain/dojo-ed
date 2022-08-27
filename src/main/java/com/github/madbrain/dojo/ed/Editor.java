package com.github.madbrain.dojo.ed;

import java.io.BufferedReader;
import java.io.IOException;

public class Editor {
    private final FileSystem fileSystem;
    private final ConsoleOutput output;

    public Editor(FileSystem fileSystem, ConsoleOutput output) {
        this.fileSystem = fileSystem;
        this.output = output;
    }

    public void enter(BufferedReader bufferedReader) throws IOException {
        throw new RuntimeException("TODO");
    }
}
