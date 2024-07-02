package org.monarchinitiative.phenopacket2prompt.international;

import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HpInternationalOboParser {


    private final Map<String, HpInternational> languageToInternationalMap;

    private static final String ENGLISH = "en";


    /**
     * Extract language acronym
     * @param annots a String such as source:value="Split hand", translation:status="OFFICIAL", source:language="en", translation:language="tr"}
     * @return in this case "tr"
     */
    public static Optional<String> getLanguage(String annots) {
        final String translation = "babelon:translation_language=\"(\\w{2,3})\"";
        final Pattern pattern = Pattern.compile(translation);
        Matcher matcher = pattern.matcher(annots);
        if (matcher.find()) {
            String language = matcher.group(1);
            return Optional.of(language);
        } else {
            return Optional.empty();
        }
    }


    public HpInternationalOboParser(File file) {
        languageToInternationalMap = new HashMap<>();
        String pattern = "id: (HP:\\d{7,7})";
        Set<String> acronyms = Set.of("cs", "en", "de", "dtp", "it", "es", "fr", "ja", "nl", "nna", "tr", "tw", "zh");
        for (String acronym : acronyms) {
            languageToInternationalMap.put(acronym, new HpInternational(acronym));
        }
        // Create a Pattern object
        Pattern idLinePattern = Pattern.compile(pattern);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean inHpTerm = false;
            TermId currentHpoTermId = null;
            while ((line = br.readLine()) != null) {
                Matcher matcher = idLinePattern.matcher(line);
                if (matcher.find()) {
                    currentHpoTermId = TermId.of(matcher.group(1));
                    inHpTerm = true;
                    //System.out.println(currentHpoTermId.getValue());
                } else if (inHpTerm) {
                    if (line.isEmpty()) {
                        inHpTerm = false;
                    } else if (line.startsWith("name:")) {
                        line = line.substring(5);
                        String [] fields = line.split("\\{");
                        if (fields.length == 1) {
                            String hpoLabel = fields[0].trim();
                            languageToInternationalMap.get(ENGLISH).addTerm(currentHpoTermId, hpoLabel);
                        } else if (fields.length == 2) {
                            String hpoLabel = fields[0].trim();
                            String annots = fields[1];
                            Optional<String> opt = getLanguage(annots);
                            if (opt.isPresent()) {
                                String language = opt.get();
                                if (! languageToInternationalMap.containsKey(language)) {
                                    System.err.println("[ERROR] Could not find language \"" + language + "\"");
                                    continue;
                                }
                                languageToInternationalMap.get(language).addTerm(currentHpoTermId, hpoLabel);
                            } else {
                                System.err.printf("[ERROR] Could not extract language for %s.\n", line);
                            }
                        }
                    }
                }
               // System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*for (String language : languageToInternationalMap.keySet()) {
            System.out.println(language);
            HpInternational international = languageToInternationalMap.get(language);
            for (var entry : international.getTermIdToLabelMap().entrySet()) {
                System.out.printf("\t%s: %s\n", entry.getKey().getValue(), entry.getValue());
            }
        }*/
    }

    public Map<String, HpInternational> getLanguageToInternationalMap() {
        return languageToInternationalMap;
    }
}
