package org.monarchinitiative.phenopacket2prompt.nejm;


import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.AdditionalConcept;
import org.monarchinitiative.phenopacket2prompt.model.AdditionalConceptI;
import org.monarchinitiative.phenopacket2prompt.model.AdditionalReplacementConceptType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for cleaning up the text that was parsed from the NEJM case report PDF files.
 * It also extracts age and sex.
 */
public class NejmCaseReportFromPdfFilterer {
    private final Logger LOGGER = LoggerFactory.getLogger(NejmCaseReportFromPdfFilterer.class);
    /** Age of the probad, e.g. P20Y for twenty years old. This must be at the beginning of the
     * parsed NEJM file, e.g.
     * age: 20
     * sex: M
     */
    private final String isoAge;

    private final String phenopacketSex;
    private final List<String> caseLines;

    private final List<String> presentationWithoutDiscussionLines;

    private final List<String> allLines;

    private String diagnosis = null;
    private boolean inCase = false;
    private boolean inDifferentialDiagnosis = false;
    private boolean inActualDiagnosis = false;

    /**
     * Match phrases such as Dr. Andrea L . Ciaranello’s Diagnosis
     */
    private static final Pattern DIAGNOSIS_REGEX = Pattern.compile("Dr. (.*) Diagnosis");


    /**
     *
     * lines such as Case 40-2022
     */
    private static final Pattern CASE_LINE_REGEX = Pattern.compile("Case \\d+-20\\d{2}");
    /**
     * Additional contents not picked up by fenomial text parsing, but added manually at the top of the
     * input file.
     */
    private final Set<AdditionalConceptI> additionalConcepts;

    public NejmCaseReportFromPdfFilterer(String caseId, List<String> lines) {
        isoAge = getIso8601Age(lines.get(0));
        phenopacketSex = getSex(lines.get(1));
        caseLines = new ArrayList<>();
        allLines = new ArrayList<>();
        int index = 2;
        boolean in_clinical_vignette = false;
        this.additionalConcepts = new HashSet<>();
        LOGGER.trace("Filterer for {}", caseId);
        while (! in_clinical_vignette) {
            String line = lines.get(index);
            index++;
            if (! line.contains(":")) {
                continue; // skip empty lines
            }
            if (line.equals("begin_vignette:")) {
                in_clinical_vignette = true;
                break;
            }
            String [] fields = line.split(":");
            if (fields.length < 2) {
                throw new PhenolRuntimeException("Malformed header line: " + line);
            } else if (fields.length == 2){
                String payload = fields[0].trim();
                String category = fields[1].trim();
                additionalConcepts.add(AdditionalConcept.of(category, payload));
            } else if (fields.length == 3) {
                String payload = fields[0].trim();
                String category = fields[1].trim();
                String replacement = fields[2].trim();
                additionalConcepts.add(AdditionalReplacementConceptType.of(category, payload, replacement));
            }
        }
        if (! in_clinical_vignette) {
            throw new PhenolRuntimeException("Did not find \"begin_vignette:\" line!");
        }
        if (index >= lines.size()) {
            throw new PhenolRuntimeException("Did not find text after \"begin_vignette:\" line!");
        }
        for (String line : lines.subList(index, lines.size())) {
            Matcher caseLineMatcher = CASE_LINE_REGEX.matcher(line);
            if (caseLineMatcher.find()) {
                continue;
            }
            // skip lines such as
            // Michael Levy, M.D., Ph.D., Bart K. Chwalisz, M.D., Benjamin M. Kozak, M.D.,  Michael K. Yoon, M.D., Helen A. Shih, M.D., and Anna M. Stagner, M.D.
            int countMD = countMd(line);
            if (countMD > 2) {
                continue;
            }
            if (line.contains("Presentation of Case")) {
                inCase = true;
            } else if (line.startsWith("Differential Diagnosis")) {
                inDifferentialDiagnosis = true;
            } else if (caseId.equalsIgnoreCase("PMID:34437787") &&
                    line.startsWith("Discussion of Bone Marrow Biopsy Results")) {
                inDifferentialDiagnosis = true;
            } else if (caseId.equalsIgnoreCase("PMID:36383716") &&
                    line.startsWith("Pathological Diagnosis")){
                inDifferentialDiagnosis = true;
            } else if (caseId.equalsIgnoreCase("PMID:33730458") &&
                    line.startsWith("Pathological Discussion")) {
                inDifferentialDiagnosis = true;
            } else if (caseId.equals("PMID:34437787") && line.startsWith("Dr. Andrew M. Crabbe")) {
                inDifferentialDiagnosis = true;
            } else {
                if (inCase && ! inDifferentialDiagnosis) {
                    caseLines.add(line);
                }
            }
            Matcher m = DIAGNOSIS_REGEX.matcher(line);
            if (m.find()) {
                inActualDiagnosis = true;
                diagnosis = lines.get(index+1);
            } else if (
                    line.startsWith("Pathological Diagnosis")) {
                inActualDiagnosis = true;
                diagnosis = lines.get(index + 1);
            } else if (line.strip().startsWith("Final Diagnosis")) {
                inActualDiagnosis = true;
                diagnosis = lines.get(index+1);
            } else if (line.strip().startsWith("Anatomical Diagnosis")) {
                inActualDiagnosis = true;
                diagnosis = lines.get(index+1);
            }


            // leave out lines after the actual diagnosis line
            if (inCase && ! inActualDiagnosis) {
                allLines.add(line);
            }

            index++;
        }
        // The purpose of the following lines is the following.
        // The case reports start with one doctor's report, e..,
        // Dr. Natalie A. Diacovo (Pediatrics):
        // After the initial presentation, there is a dicussion amongst
        // a group of doctors. The discussion begins with text from
        // a second doctor, e.g.
        // Dr. Maria G. Figueiro Longo:
        // We want to extract the text "between the doctors --
        // this is the initial presentation of the case
        String caseLinesStr = String.join("\n", caseLines);
        var pattern = Pattern.compile("Dr. (.*?):");
        var matcher = pattern.matcher(caseLinesStr);
        int n_matched = 0;
        int start=-1;
        int end=-1;
        while(matcher.find()) {
                n_matched++;
            if (n_matched==1){
                start = matcher.end()+1;
            } else if (n_matched==2) {
                end = matcher.start() -1;
            }
        }
        if (n_matched==1) {
            caseLinesStr = caseLinesStr.substring(start);
        } else if (n_matched>1) {
            caseLinesStr = caseLinesStr.substring(start, end);
        }
        var plines = caseLinesStr.split("\\n");
        presentationWithoutDiscussionLines = Arrays.stream(plines).toList();
    }


    private int countMd(String line) {
        String findString = "M.D.";
        return line.split(findString, -1).length-1;
    }

    /**
     *
     * @param age a line such as age: 26
     * @return an iso8601 duration string
     */
    String getIso8601Age(String age) {
        if (! age.startsWith("age:") && (! age.startsWith("Age:"))) {
            throw new PhenolRuntimeException("[NejmCaseReportFromPdfFilterer] Malformed age line: " + age);
        }
        String years = age.substring(4).trim();
        years = years.replace(".", "");// remove stray period
        if (years.equalsIgnoreCase("newborn")) {
            return "P0Y0M1D";
        }
        if (years.contains("12-month-old")) {
            return "P1Y";
        }
        int y = Integer.parseInt(years);
        return String.format("P%dY", y);
    }

    /**
     *
     * @param sex a line such as sex: female
     * @return "MALE" or "FEMALE"
     */
    String getSex(String sex) {
        if (! sex.toLowerCase().startsWith("sex:") ) {
            throw new PhenolRuntimeException("Malformed sex line: " + sex);
        }
        String s = sex.substring(4).trim();
        s = s.replace(".","");
        if (s.equalsIgnoreCase("female")) {
            return "FEMALE";
        } else if (s.equalsIgnoreCase("male")) {
            return "MALE";
        } else if (s.equalsIgnoreCase("boy")) {
            return "MALE";
        } else {
            throw  new PhenolRuntimeException("[NejmCaseReportFromPdfFilterer] Malformed sex line: " + sex);
        }
    }




    public String getIsoAge() {
        return isoAge;
    }

    public String getPhenopacketSex() {
        return phenopacketSex;
    }

    public List<String> getCaseLines() {
        return caseLines;
    }

    public List<String> getPresentationWithoutDiscussionLines() {
        return presentationWithoutDiscussionLines;
    }

    public List<String> getAllLines() {
        return allLines;
    }

    public Optional<String> getDiagnosis() {
        return Optional.ofNullable(this.diagnosis);
    }

    public Set<AdditionalConceptI> getAdditionalConcepts() {
        return additionalConcepts;
    }

    public boolean validParse() {
        return this.inCase && this.inDifferentialDiagnosis && inActualDiagnosis;
    }
}

