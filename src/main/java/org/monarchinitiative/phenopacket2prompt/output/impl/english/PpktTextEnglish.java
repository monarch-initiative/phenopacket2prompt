package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PpktTextEnglish implements PhenopacketTextGenerator {
    @Override
    public String LLM_PROMPT_HEADER() {
        try {
            return Files.readString(Path.of("src/main/resources/prompts/headers/english_header.txt"));
        } catch (IOException e) {
            return "Header not found for english.";
        }
    }

    @Override
    public String LLM_PROMPT_FOOTER() {
        try {
            return Files.readString(Path.of("src/main/resources/prompts/headers/english_footer.txt"));
        } catch (IOException e) {
            return "Header not found for english.";
        }
    }
}
