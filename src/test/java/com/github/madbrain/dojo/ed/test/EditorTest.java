package com.github.madbrain.dojo.ed.test;

import com.github.madbrain.dojo.ed.ConsoleOutput;
import com.github.madbrain.dojo.ed.Editor;
import com.github.madbrain.dojo.ed.FileSystem;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class EditorTest {

    private final TestFileSystem fileSystem = new TestFileSystem();
    private final TestConsoleOutput consoleOutput = new TestConsoleOutput();
    private final Editor editor = new Editor(fileSystem, consoleOutput);

    private static class TestFileSystem implements FileSystem {
        private final Map<String, String> files = new HashMap<>();

        @Override
        public String read(String filename) {
            return files.get(filename);
        }

        @Override
        public void write(String filename, String content) {
            this.files.put(filename, content);
        }
    }

    private static class TestConsoleOutput implements ConsoleOutput {
        private final List<String> outputLines = new ArrayList<>();

        @Override
        public void println(String content) {
            outputLines.add(content);
        }
    }

    private BufferedReader lines(String... lines) {
        return new BufferedReader(new StringReader(Arrays.stream(lines)
                .map(line -> line + "\n")
                .collect(Collectors.joining())));
    }

    @Test
    public void testBadCommand() throws IOException {
        editor.enter(lines("xx"));
        assertThat(consoleOutput.outputLines).containsExactly("?");
    }

    @Test
    public void testAppendAndWrite() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "w junk"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly("68");
        assertThat(fileSystem.files.get("junk")).isEqualTo(
                "Now is the time\n" +
                "for all good men\n" +
                "to come to the aid of their party.\n");
    }

    @Test
    public void testQuit() throws IOException {
        // WHEN
        editor.enter(lines("q",
                "Now is the time"));
        assertThat(consoleOutput.outputLines).isEmpty();
    }

    @Test
    public void testEdit() throws IOException {
        // GIVEN
        fileSystem.files.put("junk",
                "Now is the time\n" +
                        "for all good men\n" +
                        "to come to the aid of their party.\n");

        // WHEN
        editor.enter(lines("a", "ignored", ".",
                "e junk",
                "a",
                "new line",
                ".",
                "w",
                "q"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly("68", "77");
        assertThat(fileSystem.files.get("junk")).isEqualTo(
                "Now is the time\n" +
                "for all good men\n" +
                "to come to the aid of their party.\n" +
                "new line\n");
    }

    @Test
    public void testWriteUnknown() throws IOException {
        // WHEN
        editor.enter(lines("w"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly("?");
    }

    @Test
    public void testEditUnknownFile() throws IOException {
        // WHEN
        editor.enter(lines("a", "ignored", ".",
                "e junk"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly("?");
    }

    @Test
    public void testGetFilename() throws IOException {
        // GIVEN
        fileSystem.files.put("junk",
                "Now is the time\n" +
                        "for all good men\n" +
                        "to come to the aid of their party.\n");

        // WHEN
        editor.enter(lines("e junk",
                "f"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly("68", "junk");
    }

    @Test
    public void testGetUnknownFilename() throws IOException {
        // WHEN
        editor.enter(lines("f"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly("?");
    }

    @Test
    public void testRead() throws IOException {
        // GIVEN
        fileSystem.files.put("junk",
                "Now is the time\n" +
                        "for all good men\n" +
                        "to come to the aid of their party.\n");

        // WHEN
        editor.enter(lines("a",
                "new line",
                ".",
                "r junk",
                "w",
                "q"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly("68", "77");
        assertThat(fileSystem.files.get("junk")).isEqualTo(
                "new line\n" +
                "Now is the time\n" +
                "for all good men\n" +
                "to come to the aid of their party.\n");
    }

    @Test
    public void testPrint() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "to come to the aid of their party.");
    }

    @Test
    public void testPrintInterval() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1,2p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly("Now is the time",
                "for all good men");
    }

    @Test
    public void testPrintEndInterval() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1,$p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly("Now is the time",
                "for all good men",
                "to come to the aid of their party.");
    }

    @Test
    public void testPrintLastLine() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "$,$p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "to come to the aid of their party.");
    }

    @Test
    public void testPrintLastLineSimple() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "$p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "to come to the aid of their party.");
    }

    @Test
    public void testPrintSingleLine() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "Now is the time");
    }

    /**
     * If the command "p" is not present real ed seems to only consider start line and ignore end line
     */
    @Test
    public void testOptionalPrintCommand() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1,2"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "Now is the time");
    }

    @Test
    public void testSubtractLine() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "$-2+1"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "for all good men");
    }

    @Test
    public void testPrintDot() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1",
                ".,$p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "Now is the time",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.");
    }

    @Test
    public void testPrintDotOp() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1",
                ".+1"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "Now is the time",
                "for all good men");
    }

    @Test
    public void testPrintBadInterval() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1",
                ".-1,.-3"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "Now is the time",
                "?");
    }

    @Test
    public void testPrintCurrentLinePosition() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                ".="));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "3");
    }

    @Test
    public void testDeleteLines() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1,2d",
                ".="));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "1");
    }

    @Test
    public void testSubstituteLines() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is th time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1s/th/the/",
                "p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "Now is the time");
    }

    @Test
    public void testSubstituteAndPrintCombined() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is th time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1s/th/the/p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "Now is the time");
    }

    @Test
    public void testSubstituteForRemove() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Nowxx is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1s/xx//p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "Now is the time");
    }

    @Test
    public void testGlobalSubstitute() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "the other side of the coin",
                ".",
                "s/the/on the/gp"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "on the oon ther side of on the coin");
    }

    @Test
    public void testSubstituteAlternateDelimiter() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Nowxx is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1s@xx@@p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "Now is the time");
    }

    @Test
    public void testSearch() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "/their/"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "to come to the aid of their party.");
    }

    @Test
    public void testSearchAndSubstitute() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "/Now/+1s/good/bad/p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "for all bad men");
    }

    @Test
    public void testRepeatLastSearch() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "/the/",
                "//"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "Now is the time",
                "to come to the aid of their party.");
    }

    @Test
    public void testChangeLines() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1c",
                "Now the time",
                "is come",
                ".",
                "1,3p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "Now the time",
                "is come",
                "for all good men");
    }

    @Test
    public void testInsertLines() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1i",
                "Well",
                ".",
                "1,3p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "Well",
                "Now is the time",
                "for all good men");
    }

    @Test
    public void testAppendInMiddle() throws IOException {
        // WHEN
        editor.enter(lines("a",
                "Now is the time",
                "for all good men",
                "to come to the aid of their party.",
                ".",
                "1a",
                "--",
                ".",
                ".=",
                "1,3p"));

        // THEN
        assertThat(consoleOutput.outputLines).containsExactly(
                "2",
                "Now is the time",
                "--",
                "for all good men");
    }

}
