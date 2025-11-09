package org.monarchinitiative.phenopacket2prompt.international;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HpInternationalBabelonParser {

    private final Map<String, HpInternational> languageToInternationalMap;

    private static final String ENGLISH = "en";


    public HpInternationalBabelonParser(File jsonFile) throws IOException {
        languageToInternationalMap = new HashMap<>();
        Set<String> acronyms = Set.of("cs", "en", "de", "dtp", "es", "fr", "it", "ja", "nl", "nna", "pt", "tr", "tw", "zh");
        for (String acronym : acronyms) {
            languageToInternationalMap.put(acronym, new HpInternational(acronym));
        }
        // Use Streaming API and not Data binding or Tree model because the file is >60MB
        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(jsonFile)) {

            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalStateException("Expected start of JSON object");
            }

            // Iterate over the fields at root level
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();

                if ("translations".equals(fieldName)) {
                    parser.nextToken(); // move to START_ARRAY

                    // Iterate through each item in the array, i.e. the file
                    while (parser.nextToken() == JsonToken.START_OBJECT) {
                        String subjectId = null;
                        String predicateId = null;
                        String sourceValue = null;
                        String sourceLanguage = null;
                        String translationValue = null;
                        String translationLanguage = null;
                        String translationStatus = null;

                        // Iterate through fields inside one translation object
                        while (parser.nextToken() != JsonToken.END_OBJECT) {
                            String name = parser.getCurrentName();
                            parser.nextToken(); // move to value

                            switch (name) {
                                case "subject_id" -> subjectId = parser.getText();
                                case "predicate_id" -> predicateId = parser.getText();
                                case "source_value" -> sourceValue = parser.getText();
                                case "source_language" -> sourceLanguage = parser.getText();
                                case "translation_value" -> translationValue = parser.getText().trim();
                                case "translation_language" -> translationLanguage = parser.getText();
                                case "translation_status" -> translationStatus = parser.getText();
                                default -> parser.skipChildren(); // skip anything else
                            }
                        }
                        TermId HpoTermId = TermId.of(subjectId);
                        languageToInternationalMap.get(ENGLISH).addTerm(HpoTermId, sourceValue);

                        // Process only OFFICIAL translations and labels
                        if ("OFFICIAL".equalsIgnoreCase(translationStatus) ||
                            "CANDIDATE".equalsIgnoreCase(translationStatus) &&
                                "rdfs:label".equalsIgnoreCase(predicateId)) {
                            if (! languageToInternationalMap.containsKey(translationLanguage)) {
                                System.err.println("[ERROR] Could not find language \"" + translationLanguage + "\"");
                                continue;
                            }
                            languageToInternationalMap.get(translationLanguage).addTerm(HpoTermId, translationValue);
                        }
                    }
                } else {
                    parser.skipChildren(); // ignore non-"translations" root fields
                }
            }
        }
    }

    public Map<String, HpInternational> getLanguageToInternationalMap() {
        return languageToInternationalMap;
    }

}