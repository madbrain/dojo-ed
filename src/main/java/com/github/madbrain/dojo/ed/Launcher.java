package com.github.madbrain.dojo.ed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public class Launcher {

    private static class ConcreteFileSystem implements FileSystem {

        @Override
        public String read(String filename) {
            try {
                return Files.readString(Path.of(filename));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void write(String filename, String content) {
            try {
                Files.writeString(Path.of(filename), content, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ConcreteConsoleOutput implements ConsoleOutput {

        @Override
        public void println(String content) {
            System.out.println(content);
            System.out.flush();
        }
    }

    public static void main(String[] args) throws IOException {
       FileSystem fileSystem = new ConcreteFileSystem();
       ConsoleOutput consoleOutput = new ConcreteConsoleOutput();
       Editor editor = new Editor(fileSystem, consoleOutput);
       editor.enter(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)));
    }
}
