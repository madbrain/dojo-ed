package com.github.madbrain.dojo.ed;

import com.github.madbrain.dojo.ed.parse.BufferAccess;
import com.github.madbrain.dojo.ed.parse.CommandParser;
import com.github.madbrain.dojo.ed.parse.TokenStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Editor implements BufferAccess {
    private final FileSystem fileSystem;
    private final ConsoleOutput output;
    private final List<String> buffer = new ArrayList<>();
    private String currentFilename = null;
    private int currentLineIndex = 0;
    private String lastSearchContent = "";

    public Editor(FileSystem fileSystem, ConsoleOutput output) {
        this.fileSystem = fileSystem;
        this.output = output;
    }

    public void enter(BufferedReader bufferedReader) throws IOException {
        String commandString;
        while ((commandString = bufferedReader.readLine()) != null) {
            try {
                Command command = parseCommand(commandString);
                if (!executeCommand(command, bufferedReader)) {
                    break;
                }
            } catch (RuntimeException e) {
                output.println("?");
            }
        }
    }

    private boolean executeCommand(Command command, BufferedReader bufferedReader) throws IOException {
        if (command.command().equals("a")) {
            String line;
            int index = command.interval().start() + 1;
            while (!(line = bufferedReader.readLine()).equals(".")) {
                buffer.add(index - 1, line);
                ++index;
            }
            currentLineIndex = index - 1;
        } else if (command.command().equals("c")) {
            String line;
            int index = command.interval().start();
            while (!(line = bufferedReader.readLine()).equals(".")) {
                if (index <= command.interval().end()) {
                    buffer.set(index - 1, line);
                } else {
                    buffer.add(index - 1, line);
                }
                ++index;
            }
            currentLineIndex = index - 1;
        } else if (command.command().equals("i")) {
            String line;
            int index = command.interval().start();
            while (!(line = bufferedReader.readLine()).equals(".")) {
                buffer.add(index - 1, line);
                ++index;
            }
            currentLineIndex = index - 1;
        } else if (command.command().equals("w") || command.command().startsWith("w ")) {
            String content = buffer.stream()
                    .map(line -> line + "\n")
                    .collect(Collectors.joining());
            String filename = command.command().length() > 2 ? command.command().substring(2).strip() : currentFilename;
            if (filename == null) {
                output.println("?");
            } else {
                currentFilename = filename;
                fileSystem.write(filename, content);
                output.println(String.valueOf(content.length()));
            }
        } else if (command.command().equals("p") || command.command().isEmpty()) {
            int start = command.interval().start();
            int end = command.command().isEmpty() ? start : command.interval().end();
            printInterval(new Interval(start, end));
        } else if (command.command().equals("d")) {
            if (!command.interval().check(this)) {
                output.println("?");
            } else {
                buffer.subList(command.interval().start() + 1, command.interval().end()).clear();
                currentLineIndex = command.interval().start();
            }
        } else if (command.command().startsWith("s")) {
            if (!command.interval().check(this)) {
                output.println("?");
            } else {
                boolean substituteSomething = false;
                char delimiter = command.command().charAt(1);
                String[] parts = command.command().substring(2).split(String.valueOf(delimiter));
                boolean isGlobal = parts.length >= 3 && parts[2].contains("g");
                for (int i = command.interval().start(); i <= command.interval().end(); ++i) {
                    String line = buffer.get(i-1);
                    StringBuilder newLine = new StringBuilder();
                    int lastIndex = 0;
                    while (lastIndex < line.length()) {
                        int pos = line.indexOf(parts[0], lastIndex);
                        if (pos >= 0) {
                            if (pos > lastIndex) {
                                newLine.append(line, lastIndex, pos);
                            }
                            newLine.append(parts[1]);
                            substituteSomething = true;
                            lastIndex = pos + parts[0].length();
                            if (!isGlobal) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    if (lastIndex < line.length()) {
                        newLine.append(line, lastIndex, line.length());
                    }
                    buffer.set(i - 1, newLine.toString());
                }
                if (substituteSomething) {
                    currentLineIndex = command.interval().end();
                    if (parts.length >= 3 && parts[2].contains("p")) {
                        printInterval(new Interval(currentLineIndex, currentLineIndex));
                    }
                } else {
                    output.println("?");
                }
            }
        } else if (command.command().startsWith("e ")) {
            String filename = command.command().substring(2).strip();
            currentFilename = filename;
            String content = fileSystem.read(filename);
            if (content == null) {
                output.println("?");
            } else {
                buffer.clear();
                buffer.addAll(Arrays.asList(content.split("\n")));
                output.println(String.valueOf(content.length()));
                currentLineIndex = buffer.size();
            }
        } else if (command.command().startsWith("r ")) {
            String filename = command.command().substring(2).strip();
            currentFilename = filename;
            String content = fileSystem.read(filename);
            if (content == null) {
                output.println("?");
            } else {
                buffer.addAll(Arrays.asList(content.split("\n")));
                output.println(String.valueOf(content.length()));
                currentLineIndex = buffer.size();
            }
        } else if (command.command().equals("f")) {
            output.println(Optional.ofNullable(currentFilename).orElse("?"));
        } else if (command.command().equals("q")) {
            return false;
        } else if (command.command().equals("=")) {
            output.println(String.valueOf(currentLineIndex));
        } else {
            output.println("?");
        }
        return true;
    }

    private void printInterval(Interval interval) {
        if (! interval.check(this)) {
            output.println("?");
        } else {
            IntStream.range(interval.start(), interval.end() + 1)
                    .mapToObj(i -> buffer.get(i-1))
                    .forEach(output::println);
            currentLineIndex = interval.end();
        }
    }

    private Command parseCommand(String command) {
        TokenStream stream = new TokenStream(command);
        return new CommandParser(stream, this).parse();
    }

    @Override
    public int currentLine() {
        return currentLineIndex;
    }

    @Override
    public int lastLineIndex() {
        return buffer.size();
    }

    @Override
    public int searchLine(String content) {
        if (content.isEmpty()) {
            content = lastSearchContent;
        }
        if (! content.isEmpty()) {
            lastSearchContent = content;
            List<Interval> intervals = new ArrayList<>();
            if (currentLineIndex + 1 <= buffer.size()) {
                intervals.add(new Interval(currentLineIndex + 1, buffer.size()));
            }
            intervals.add(new Interval(1, currentLineIndex));
            for (Interval interval : intervals) {
                for (int i = interval.start(); i <= interval.end(); ++i) {
                    String line = buffer.get(i - 1);
                    if (line.contains(content)) {
                        currentLineIndex = i;
                        return i;
                    }
                }
            }
        }
        return 0;
    }
}
