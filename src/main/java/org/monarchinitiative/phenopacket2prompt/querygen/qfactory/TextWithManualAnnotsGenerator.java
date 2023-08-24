package org.monarchinitiative.phenopacket2prompt.querygen.qfactory;

import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.model.AdditionalConceptI;
import org.monarchinitiative.phenopacket2prompt.model.AdditionalConceptType;
import org.monarchinitiative.phenopacket2prompt.nejm.NejmCaseReportFromPdfFilterer;
import org.monarchinitiative.phenopacket2prompt.querygen.TimePoint;
import org.monarchinitiative.phenopacket2prompt.querygen.TimePointParser;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.phenopackets.schema.v2.core.PhenotypicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TextWithManualAnnotsGenerator extends AbstractQueryGenerator {
    private final Logger LOGGER = LoggerFactory.getLogger(TextWithManualAnnotsGenerator.class);
    private final String promptText;

    private final Set<AdditionalConceptI> additionalConcepts;
    private final Set<String> pmh;
    private final Set<String> familyHistory;

    private final List<String> outputLines;


    public TextWithManualAnnotsGenerator(NejmCaseReportFromPdfFilterer filterer, String id, TermMiner miner, Ontology hpo) {
        super(filterer, id, miner, hpo);
        this.outputLines = new ArrayList<>();
        this.pmh = new HashSet<>();
        familyHistory = filterer.getAdditionalConcepts().stream()
                        .filter(a -> a.conceptType() == AdditionalConceptType.FAMILY_HISTORY)
                        .map(AdditionalConceptI::insertText)
                        .collect(Collectors.toSet());
        this.additionalConcepts = filterer.getAdditionalConcepts();
        String phenotext = getPhenopacketTextWithManualAdditions();
        promptText = String.format("%s%s", QUERY_HEADER, phenotext);
    }



    protected String getPhenopacketTextWithManualAdditions() {
        TimePointParser timePointParser = new TimePointParser();
        List<String> lines = filterer.getPresentationWithoutDiscussionLines();
        String vignette = String.join(" ", lines);
        // the next five lines extract the first sentence from the text -- we include the first sentence verbatim
        // in our query prompts
        int ii = vignette.indexOf(".");
        if (ii < 0) {
            throw new PhenolRuntimeException("Malformed vignette without one single period");
        }
        String firstSentence = vignette.substring(0, ii + 1).strip();
        vignette = vignette.substring(ii + 1);
        List<TimePoint> timePointList = timePointParser.getTimePoints(vignette);
        System.out.printf("Vignatte includes nitroglycerin? %s\n", vignette.contains("nitroglycerin"));
        try {
            for (var tseg : timeSegments(vignette, timePointList)) {
                String timePoint = tseg.getTimeDesgination();//entry.getKey();
                String vignette_at_timepoint = tseg.getPayload();//entry.getValue();
                System.out.printf("Vignatte AT TP  includes nitroglycerin? %s\n", vignette_at_timepoint.contains("nitroglycerin"));
                if (vignette_at_timepoint.equals("Examination was notable for")) {
                    vignette_at_timepoint = "On examination ";
                }
                if (vignette_at_timepoint.length() > MIN_DESCRIPTION_LENGTH) {
                    String output = getPhenopacketBasedQuerySegmentWithAdditions(timePoint, vignette_at_timepoint);
                    if (output.isEmpty()) continue;
                    outputLines.add(output.trim());
                }
            }
        } catch (Exception eee) {
            System.out.printf("[ERROR(TextPlusManualGenerator.java] Could not parse time segments for because of %s",  eee.getMessage());
            System.exit(1);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(firstSentence).append("\n");
        if (pmh.size() > 0) {
            sb.append("The past medical history was notable for ")
                    .append(getOxfordCommaList(pmh))
                    .append("\n");
        }
        if (familyHistory.size() > 0) {
            sb.append("The family history was notable for the following. ");
            for (String item: familyHistory) {
                sb.append(item).append("\n");
            }
        }
        for (var line : outputLines) {
            sb.append(line);
        }
        return sb.toString();
    }


    protected String getPhenopacketBasedQuerySegmentWithAdditions(String presentationTimeDescription, String vignette_at_timepoint) {
        List<PhenotypicFeature> pfeatures = getPhenotypicFeatures(vignette_at_timepoint);
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
            String x = addcon.insertText();
            // check if the vignette for the current time period includes a text from the
            // additional concepts listed at the top of the input file
            // e.g.  if the input file has
            // Cough:PHENOTYPTE and we find the string "Cough" in the original_vignette_text,
            // then we would add "Cough" to the set observed_terms
            // if the input file has
            // Aspirin:TREATMENT, then we add "Aspirin" to the set treatment
            if (vignette_at_timepoint.contains(addcon.originalText())) {
                switch (addcon.conceptType()) {
                    case PHENOTYPE -> observed_terms.add(addcon.insertText());
                    case EXCLUDE -> excluded_terms.add(addcon.insertText());
                    case DIAGNOSTICS -> diagnostics.add(addcon.insertText());
                    case TREATMENT -> treatment.add(addcon.insertText());
                    case VERBATIM -> verbatim.add(addcon.insertText());
                    case PMH -> {
                        // do not repeat the PMH even if the original text mentions it more than once
                        if (!pmh.contains(addcon.originalText())) {
                            pmh.add(addcon.insertText());
                        }
                    }
                    case FAMILY_HISTORY -> familyHistory.add(addcon.insertText());
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

        sb.append(capitalizedTimepoint).append(" ");
        boolean observedEmpty = true;
        boolean needEmpty = true;
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
            if (needEmpty) { sb.append(" "); needEmpty = false; }
            String excludededSymptoms = getOxfordCommaList(excluded_terms);
            if (observedEmpty) {
                sb.append("The following signs and symptoms were excluded: ");
            } else {
                sb.append("The following signs and symptoms were excluded: ");
            }
            sb.append(excludededSymptoms).append("\n");
        }
        if (! diagnostics.isEmpty()) {
            if (needEmpty) { sb.append(" "); needEmpty = false; }
            sb.append("The following diagnostic observations were made: ");
            sb.append(getOxfordCommaList(diagnostics));
            sb.append("\n");
        }
        if (! treatment.isEmpty()) {
            if (needEmpty) { sb.append(" "); needEmpty = false; }
            sb.append("The following treatments were administered: ");
            sb.append(getOxfordCommaList(treatment));
            sb.append("\n");
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
