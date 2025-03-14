package org.monarchinitiative.phenopacket2prompt.output.impl.turkish;

import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PpktTextTurkish implements PhenopacketTextGenerator {
    @Override
    public String LLM_PROMPT_HEADER() {
        try {
            return Files.readString(Path.of("src/main/resources/prompts/headers/turkish_header.txt"));
        } catch (IOException e) {
            return "Header not found for turkish.";
        }
    }

    @Override
    public String LLM_PROMPT_FOOTER() {
        try {
            return Files.readString(Path.of("src/main/resources/prompts/headers/turkish_footer.txt"));
        } catch (IOException e) {
            return "Header not found for turkish.";
        }
    }
}
