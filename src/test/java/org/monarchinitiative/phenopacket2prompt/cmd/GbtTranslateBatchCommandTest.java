package org.monarchinitiative.phenopacket2prompt.cmd;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Assertions;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;

class GbtTranslateBatchCommandTest {
    private static final Path CLI_JAR = Paths.get("target/phenopacket2prompt.jar");

    @BeforeAll
    static void checkJarExists() {
        Assumptions.assumeTrue(Files.exists(CLI_JAR),
                "Skipping CLI test — JAR file not found. Run `mvn package` first.");
    }

    static Stream<TestCase> provideTestCases() {
        return Stream.of(
                new TestCase("-p",
                        "PMID_27672653_Individual_1_PATIENTONLY_en-prompt.txt",
                        "PMID_27672653_Individual_1_PATIENTONLY_it-prompt.txt",
                        "PMID_27672653_Individual_1_PATIENTONLY_es-prompt.txt"),
                new TestCase("--only-patient-description=true",
                        "PMID_27672653_Individual_1_PATIENTONLY_en-prompt.txt",
                        "PMID_27672653_Individual_1_PATIENTONLY_it-prompt.txt",
                        "PMID_27672653_Individual_1_PATIENTONLY_es-prompt.txt") //,
                /* Rempve this test? It is not compatible with changing footers and headers...
                new TestCase("--only-patient-description=false",
                        "PMID_27672653_Individual_1_en-prompt.txt",
                        "PMID_27672653_Individual_1_it-prompt.txt",
                        "PMID_27672653_Individual_1_es-prompt.txt")*/
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void testPatientOnlyMode(TestCase testCase, @TempDir Path tempDir) throws IOException {
        // Copy test data into temp directory
        Path testDataFolder = tempDir.resolve("test-data");
        Files.createDirectory(testDataFolder);
        copyTestData("data/GCDH_test_ppkt.json", testDataFolder.resolve("GCDH_test_ppkt.json"));

        // Define output file path
        Path outputFileEn = tempDir.resolve("en/PMID_27672653_Individual_1_en-prompt.txt");
        Path outputFileIt = tempDir.resolve("it/PMID_27672653_Individual_1_it-prompt.txt");
        Path outputFileEs = tempDir.resolve("es/PMID_27672653_Individual_1_es-prompt.txt");

        // Execute the command
        CommandLine cmd = new CommandLine(new GbtTranslateBatchCommand());
        int exitCode = cmd.execute(("-d"+ " " + testDataFolder + " " + "-o" + " " + tempDir + " " + testCase.input).split(" "));

        // Verify the command exited successfully
        Assertions.assertEquals(0, exitCode, "Command should exit successfully.");

        // Read expected output
        String expectedOutputFileEn = readResourceFile("data/expected-output/" + testCase.expectedOutputFileEn);
        String expectedOutputFileIt = readResourceFile("data/expected-output/" + testCase.expectedOutputFileIt);
        String expectedOutputFileEs = readResourceFile("data/expected-output/" + testCase.expectedOutputFileEs);


        // Read actual output from the file
        String actualOutputEn = Files.readString(outputFileEn).trim();
        String actualOutputIt = Files.readString(outputFileIt).trim();
        String actualOutputEs = Files.readString(outputFileEs).trim();

        // Compare output
        Assertions.assertEquals(expectedOutputFileEn, actualOutputEn, "Output file content should match expected output.");
        Assertions.assertEquals(expectedOutputFileIt, actualOutputIt, "Output file content should match expected output.");
        Assertions.assertEquals(expectedOutputFileEs, actualOutputEs, "Output file content should match expected output.");


    }

    static class TestCase {
        String input;
        String expectedOutputFileEn;
        String expectedOutputFileIt;
        String expectedOutputFileEs;

        TestCase(String input, String expectedOutputFileEn, String expectedOutputFileIt, String expectedOutputFileEs) {
            this.input = input;
            this.expectedOutputFileEn = expectedOutputFileEn;
            this.expectedOutputFileIt = expectedOutputFileIt;
            this.expectedOutputFileEs = expectedOutputFileEs;

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
