package org.monarchinitiative.phenopacket2prompt.phenopacket;


import org.monarchinitiative.phenol.base.PhenolRuntimeException;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class processes texts like this
 * "clinic of another hospital. On examination, there was {{conjunctival injection:PHENOTYPE}} in " +
 *                 "both eyes. The {{lungs were clear on auscultation:EXCLUDED:Abnormal breath sound}}, and the remainder of the physical " +
 *                 "examination was reportedly normal. Testing of a nasopharyngeal specimen for " +
 *                 "severe acute respiratory syndrome coronavirus 2 {{(SARS-CoV-2) RNA was negative:LABORATORY}}."
 *   and removes the sections in {{ brackets }}
 *   it returns a list of OBSERVED, EXCLUDED, LABORATORY and TREATMENT and also returns the rest of the
 *   string in which the bracketed text has been removed.
 */
public class BracketParser {


    private final static Pattern pattern = Pattern.compile("(\\{\\{[^}]*}})");


    Set<String> OBSERVED = new HashSet<>();
    Set<String> EXCLUDED= new HashSet<>();;
    Set<String> DIAGNOSTIC= new HashSet<>();;
    Set<String> TREATMENT= new HashSet<>();
    Set<String> VERBATIM= new HashSet<>();


    String trimmedVignette;

    String originalPurifiedText;


    public BracketParser(String vignette) {
        int current_pos = 0;
        Matcher m = pattern.matcher(vignette);
        StringBuilder remainderBuilder = new StringBuilder();
        StringBuilder purifiedText = new StringBuilder();
        while (m.find()) {
            int pattern_start = m.start();
            int pattern_end = m.end();
            processBracketed(m.group());
            remainderBuilder.append(vignette.substring(current_pos, pattern_start));
            purifiedText.append(vignette.substring(current_pos, pattern_start));
            String original = getOriginalFromBracket(m.group());
            purifiedText.append(original);
            current_pos = pattern_end + 1;
        }
        if (current_pos < vignette.length()) {
            remainderBuilder.append(vignette.substring(current_pos));
            purifiedText.append(vignette.substring(current_pos));
        }
        this.trimmedVignette = remainderBuilder.toString();
        this.originalPurifiedText = purifiedText.toString();

    }

    private String getOriginalFromBracket(String bracketedText) {
        if (! bracketedText.startsWith("{{")) {
            throw new PhenolRuntimeException("Malformed forward bracket " + bracketedText);
        }
        bracketedText = bracketedText.substring(2);
        if (! bracketedText.endsWith("}}")) {
            throw new PhenolRuntimeException("Malformed backward bracket " + bracketedText);
        }
        bracketedText = bracketedText.substring(0, bracketedText.length() - 2);
        String [] fields = bracketedText.split(":");
        return fields[0];
    }

    private void processBracketed(String bracketedText) {
        if (! bracketedText.startsWith("{{")) {
            throw new PhenolRuntimeException("Malformed forward bracket " + bracketedText);
        }
        bracketedText = bracketedText.substring(2);
        if (! bracketedText.endsWith("}}")) {
            throw new PhenolRuntimeException("Malformed backward bracket " + bracketedText);
        }
        bracketedText = bracketedText.substring(0, bracketedText.length() - 2);
        String [] fields = bracketedText.split(":");
        if (fields.length >= 2) {
            var payload = fields[0].trim();
            var category = fields[1].trim();
            if (fields.length == 3) {
                payload = fields[2].trim();
            }
            switch (category) {
                case "PHENOTYPE" -> OBSERVED.add(payload);
                case "EXCLUDED" -> EXCLUDED.add(payload);
                case "TREATMENT" -> TREATMENT.add(payload);
                case "DIAGNOSTICS" -> DIAGNOSTIC.add(payload);
                case "VERBATIM" -> VERBATIM.add(payload);
                default -> {
                    throw new PhenolRuntimeException("Malformed category (" + category +") for payload (" + payload + ")");
                }
            }
        } else {
            throw new PhenolRuntimeException("Malformed bracket - no :" + bracketedText);
        }
    }

    public String getTrimmedVignette() {
        return trimmedVignette;
    }


    public Set<String> getOBSERVED() {
        return OBSERVED;
    }

    public Set<String> getEXCLUDED() {
        return EXCLUDED;
    }

    public Set<String> getVERBATIM() {
        return VERBATIM;
    }

    public Set<String> getDIAGNOSTIC() {
        return DIAGNOSTIC;
    }

    public Set<String> getTREATMENT() {
        return TREATMENT;
    }
}
