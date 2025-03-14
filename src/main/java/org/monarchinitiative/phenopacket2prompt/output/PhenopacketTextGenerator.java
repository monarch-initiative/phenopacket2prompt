package org.monarchinitiative.phenopacket2prompt.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface PhenopacketTextGenerator {


    default String LLM_PROMPT_HEADER(String language) {
        try {
            return Files.readString(Path.of("src/main/resources/prompts/headers/" + language +"_header.txt"));
        } catch (IOException e) {
            return "";
        }
    }

    default String LLM_PROMPT_FOOTER(String language) {
        try {
            return Files.readString(Path.of("src/main/resources/prompts/headers/" + language + "footer.txt"));
        } catch (IOException e) {
            return "";
        }
    }

}
