package org.monarchinitiative.phenopacket2prompt.cmd;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Assertions;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;

class GbtTranslateBatchCommandTest {

    static Stream<TestCase> provideTestCases() {
        return Stream.of(
                new TestCase("-p", "PMID_27672653_Individual_1_PATIENTONLY_en-prompt.txt"),
                new TestCase("--only-patient-description=true", "PMID_27672653_Individual_1_PATIENTONLY_en-prompt.txt"),
                new TestCase("--only-patient-description=false", "PMID_27672653_Individual_1_en-prompt.txt")
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void testNewOption(TestCase testCase, @TempDir Path tempDir) throws IOException {
        // Copy test data into temp directory
        Path testDataFolder = tempDir.resolve("test-data");
        Files.createDirectory(testDataFolder);
        copyTestData("data/GCDH_test_ppkt.json", testDataFolder.resolve("GCDH_test_ppkt.json"));

        // Define output file path
        Path outputFile = tempDir.resolve("en/PMID_27672653_Individual_1_en-prompt.txt"); // issue here?

        // Execute the command
        CommandLine cmd = new CommandLine(new GbtTranslateBatchCommand());
        int exitCode = cmd.execute(("-d"+ " " + testDataFolder + " " + "-o" + " " + tempDir + " " + testCase.input).split(" "));

        // Verify the command exited successfully
        Assertions.assertEquals(0, exitCode, "Command should exit successfully.");

        // Read expected output
        String expectedOutput = readResourceFile("data/expected-output/" + testCase.expectedOutputFile);

        // Read actual output from the file
        String actualOutput = Files.readString(outputFile).trim();

        // Compare output
        Assertions.assertEquals(expectedOutput, actualOutput, "Output file content should match expected output.");
    }

    static class TestCase {
        String input;
        String expectedOutputFile;

        TestCase(String input, String expectedOutputFile) {
            this.input = input;
            this.expectedOutputFile = expectedOutputFile;
        }

        @Override
        public String toString() {
            return "Input: " + input;
        }
    }

    private static void copyTestData(String resourcePath, Path targetPath) throws IOException {
        InputStream in = GbtTranslateBatchCommandTest.class.getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) throw new FileNotFoundException("Test resource not found: " + resourcePath);
        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private static String readResourceFile(String resourcePath) throws IOException {
        InputStream in = GbtTranslateBatchCommandTest.class.getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) throw new FileNotFoundException("Expected output file not found: " + resourcePath);
        return new String(in.readAllBytes()).trim();
    }
}
