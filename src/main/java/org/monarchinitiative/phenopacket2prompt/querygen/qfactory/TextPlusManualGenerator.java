package org.monarchinitiative.phenopacket2prompt.querygen.qfactory;

import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.model.AdditionalConceptI;
import org.monarchinitiative.phenopacket2prompt.nejm.NejmCaseReportFromPdfFilterer;
import org.monarchinitiative.phenopacket2prompt.querygen.TimePoint;
import org.monarchinitiative.phenopacket2prompt.querygen.TimePointParser;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.phenopackets.schema.v2.core.PhenotypicFeature;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TextPlusManualGenerator extends AbstractQueryGenerator {

    private final String promptText;

    private final Set<AdditionalConceptI> additionalConcepts;

    public  TextPlusManualGenerator(NejmCaseReportFromPdfFilterer filterer, String id, TermMiner miner, Ontology hpo) {
        super(filterer, id, miner, hpo);
        this.additionalConcepts = filterer.getAdditionalConcepts();
        String intro = getPersonIntroduction();
        String phenotext = getPhenopacketTextWithAdditions();
        promptText = String.format("%s%s%s", QUERY_HEADER, intro, phenotext);
    }



    protected String getPhenopacketTextWithAdditions() {
        TimePointParser timePointParser = new TimePointParser();
        List<String> lines = filterer.getPresentationWithoutDiscussionLines();
        String vignette = String.join(" ", lines);
        int ii = vignette.indexOf(".");
        if (ii < 0) {
            throw new PhenolRuntimeException("Malformed vignette without one single period");
        }
        String firstSentence = vignette.substring(0, ii + 1).strip();
        vignette = vignette.substring(ii + 1);
        List<TimePoint> timePointList = timePointParser.getTimePoints(vignette);

        StringBuilder sb = new StringBuilder();
        sb.append(firstSentence).append("\n");
        try {
            //Map<String, String> timeSegments = timeSegments(starts, ends, vignette, start2pointMap);
            Map<String, String> timeSegments = timeSegments(vignette, timePointList);
            for (var entry : timeSegments.entrySet()) {
                String timePoint = entry.getKey();
                String description = entry.getValue();
                if (description.equals("Examination was notable for")) {
                    description = "On examination";
                }
                if (description.length() > MIN_DESCRIPTION_LENGTH) {
                    String output = getPhenopacketBasedQuerySegmentWithAdditions(timePoint, description);
                    if (output.isEmpty()) continue;
                    sb.append(output).append("\n");
                }
            }
        } catch (Exception eee) {
            System.out.printf("[ERROR(TextPlusManualGenerator.java] Could not parse time segments for because of %s",  eee.getMessage());
            System.exit(1);
        }
        return sb.toString();
    }


    protected String getPhenopacketBasedQuerySegmentWithAdditions(String presentationTimeDescription, String input) {
        List<PhenotypicFeature> pfeatures = getPhenotypicFeatures(input);
        if (pfeatures.isEmpty()) {
            return ""; // no features detected for this time period
        }
        Set<String> diagnostics = new HashSet<>();
        Set<String> treatment = new HashSet<>();
        Set<String> verbatim = new HashSet<>();
        Set<String> observed_terms = pfeatures.stream()
                .filter(Predicate.not(PhenotypicFeature::getExcluded))
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getLabel)
                .collect(Collectors.toSet());
        Set<String> excluded_terms = pfeatures.stream()
                .filter(PhenotypicFeature::getExcluded)
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getLabel)
                .collect(Collectors.toSet());
        for (var addcon : this.additionalConcepts ) {
            if (input.contains(addcon.originalText())) {
                switch (addcon.conceptType()) {
                    case PHENOTYPE -> observed_terms.add(addcon.insertText());
                    case EXCLUDE -> excluded_terms.add(addcon.insertText());
                    case DIAGNOSTICS -> diagnostics.add(addcon.insertText());
                    case TREATMENT -> treatment.add(addcon.insertText());
                    case VERBATIM -> verbatim.add(addcon.insertText());
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        String capitalizedTimepoint;
        if (presentationTimeDescription.equalsIgnoreCase("Examination was notable for")) {
            presentationTimeDescription = "On examination";
        }
        if (presentationTimeDescription.length() < 2) {
            capitalizedTimepoint = "";
        } else {
            capitalizedTimepoint = presentationTimeDescription.substring(0, 1).toUpperCase() + presentationTimeDescription.substring(1);
        }

        sb.append(capitalizedTimepoint);
        boolean observedEmpty = true;
        if (!observed_terms.isEmpty()) {
            observedEmpty = false;
            if (capitalizedTimepoint.isEmpty()) {
                sb.append("The patient presented with ");
            } else if (capitalizedTimepoint.equalsIgnoreCase("Other medical history included")) {
                sb.append(" "); // this will output Other medical history included X, Y, and Z
            } else {
                sb.append(", the patient presented with ");
            }

            String observedSymptoms = getOxfordCommaList(observed_terms);
            sb.append(observedSymptoms).append(" \n");
        }
        if (!excluded_terms.isEmpty()) {
            String excludededSymptoms = getOxfordCommaList(excluded_terms);
            if (observedEmpty) {
                sb.append(", the following signs and symptoms were excluded: ");
            } else {
                sb.append("The following signs and symptoms were excluded: ");
            }
            sb.append(excludededSymptoms).append(" ");
        }
        if (! diagnostics.isEmpty()) {
            sb.append("The following diagnostic observations were made: \n");
            sb.append(getOxfordCommaList(diagnostics));
        }
        if (! treatment.isEmpty()) {
            sb.append("The following treatments were administered: \n");
            sb.append(getOxfordCommaList(treatment));
        }
        if (! verbatim.isEmpty()) {
            for (var v : verbatim)
            sb.append(v).append("\n");
        }
        return sb.toString();
    }


    @Override
    public String getQuery() {
        return promptText;
    }
}
