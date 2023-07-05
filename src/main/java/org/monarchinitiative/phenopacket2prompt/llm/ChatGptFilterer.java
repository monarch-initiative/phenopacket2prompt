package org.monarchinitiative.phenopacket2prompt.llm;


import org.monarchinitiative.phenol.base.PhenolRuntimeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatGptFilterer {

    private final String age;
    private final String sex;

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



    public ChatGptFilterer(String caseId, List<String> lines) {
        age = lines.get(0);
        isoAge = getAge(age);
        sex = lines.get(1);
        phenopacketSex = getSex(sex);
        caseLines = new ArrayList<>();
        allLines = new ArrayList<>();
        int howManyDrDiscussions = 0;
        int index = 2;
        for (String line : lines.subList(2, lines.size())) {
            //System.out.println(line);
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
                    line.startsWith("Pathological Discussion")){
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
            } else if (caseId.equalsIgnoreCase("PMID:34437787") &&
                    line.startsWith("Pathological Diagnosis")) {
                inActualDiagnosis = true;
                diagnosis = lines.get(index + 1);
            } else if (caseId.equalsIgnoreCase("PMID:36383716") &&
                    line.startsWith("Pathological Diagnosis")) {
                inActualDiagnosis = true;
                diagnosis = lines.get(index + 1);
            } else if (caseId.equalsIgnoreCase("PMID:33730458") &&
                    line.startsWith("Pathological Diagnosis")) {
                inActualDiagnosis = true;
                diagnosis = lines.get(index + 1);
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
    String getAge(String age) {
        if (! age.startsWith("age:") && (! age.startsWith("Age:"))) {
            throw new PhenolRuntimeException("Malformed age line: " + age);
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
            throw  new PhenolRuntimeException("Malformed sex line: " + sex);
        }
    }


    public String getAge() {
        return age;
    }

    public String getSex() {
        return sex;
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


    public boolean validParse() {
        return this.inCase && this.inDifferentialDiagnosis && inActualDiagnosis;
    }
}

