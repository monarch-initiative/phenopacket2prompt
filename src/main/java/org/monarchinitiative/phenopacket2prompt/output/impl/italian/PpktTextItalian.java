package org.monarchinitiative.phenopacket2prompt.output.impl.italian;

import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;


public class PpktTextItalian implements PhenopacketTextGenerator {



    @Override
    public String LLM_PROMPT_HEADER() {
        try {
            return Files.readString(Path.of("src/main/resources/prompts/headers/italian_header.txt"));
        } catch (IOException e) {
            return "Header not found for italian.";
        }
    }

    @Override
    public String LLM_PROMPT_FOOTER() {
        try {
            return Files.readString(Path.of("src/main/resources/prompts/headers/italian_footer.txt"));
        } catch (IOException e) {
            return "Header not found for italian.";
        }
    }

}
