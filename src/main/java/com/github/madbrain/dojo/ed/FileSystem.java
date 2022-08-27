package com.github.madbrain.dojo.ed;

public interface FileSystem {
    String read(String filename);

    void write(String filename, String content);
}
