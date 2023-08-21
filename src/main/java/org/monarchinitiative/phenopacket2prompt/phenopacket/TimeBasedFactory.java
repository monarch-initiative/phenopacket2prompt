package org.monarchinitiative.phenopacket2prompt.phenopacket;

import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.fenominal.model.MinedTerm;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenopacket2prompt.llm.NejmCaseReportFromPdfFilterer;
import org.phenopackets.phenopackettools.builder.builders.PhenotypicFeatureBuilder;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.phenopackets.schema.v2.core.PhenotypicFeature;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TimeBasedFactory extends QueryFactory {

    /**
     * If the description segment of a time period is less than 5 characters, skip it.
     */
    private final static int MIN_DESCRIPTION_LENGTH = 5;
    private final NejmCaseReportFromPdfFilterer filterer;

    private final TermMiner miner;

    private final Ontology hpo;

    private final String caseId;

    private final String isoAge;

    private final String phenopacketSex;

    private final String person_string;

    public TimeBasedFactory(NejmCaseReportFromPdfFilterer filterer, String id, TermMiner miner, Ontology hpo) {
        this.filterer = filterer;
        this.miner = miner;
        this.hpo = hpo;
        this.phenopacketSex = filterer.getPhenopacketSex();
        this.isoAge = filterer.getIsoAge();
        this.person_string = get_person_string();
        this.caseId = id;
    }

    private String get_person_string() {
        String sex = this.phenopacketSex.toLowerCase();
        final Pattern AGE_REGEX = Pattern.compile("P(\\d+)Y");
        Matcher m = AGE_REGEX.matcher(this.isoAge);
        if (m.find()) {
            String years = m.group(1);
            return "A " + years + "-year old " + sex;
        }
        final Pattern DAYS_REGEX = Pattern.compile("P0Y(\\d+)D");
        Matcher m2 = DAYS_REGEX.matcher(isoAge);
        if (m2.find()) {
            String years = m2.group(1);
            return "A " + years + "-day old " + sex + " newborn";
        }
        throw new PhenolRuntimeException("Could not extract person");
    }

    @Override
    public String getPhenopacketBasedQuery() {
        TimePointParser timePointParser = new TimePointParser();
        List<String> lines = filterer.getPresentationWithoutDiscussionLines();
        String vignette = String.join(" ", lines);
        int ii = vignette.indexOf(".");
        if (ii < 0) {
            throw new PhenolRuntimeException("Malformed vignette without one single period");
        }
        String firstSentence = vignette.substring(0, ii+1).strip();
        vignette = vignette.substring(ii+1);
        List<TimePoint> timePointList = timePointParser.getTimePoints(vignette);

        StringBuilder sb = new StringBuilder();
        sb.append(QUERY_HEADER);
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
                if ( description.length() > MIN_DESCRIPTION_LENGTH) {
                    String output = getPhenopacketBasedQuerySegment(timePoint, description);
                    if (output.isEmpty()) continue;
                    sb.append(output).append("\n");
                }
            }
        } catch (Exception eee) {
            System.out.printf("[ERROR(TimeBasedFactory.java] Could not parse time segments for %s because of %s", caseId, eee.getMessage());
            System.exit(1);
        }



        return sb.toString();
    }

    private Map<String, String> timeSegments(String vignette, List<TimePoint> timePointList) {
        Map<String, String> timeSegments = new LinkedHashMap<>(); // ordered map
        String nextStart = "";
        int lastEnd = 0;
        for (var timePoint: timePointList) {
            int s = timePoint.start();
            int e = timePoint.end();
            String seg = nextStart + vignette.substring(lastEnd, s);
            lastEnd = e + 1;
            timeSegments.put(nextStart, seg.strip());
            nextStart = timePoint.point();
        }
        if (lastEnd < vignette.length()) {
            String seg = nextStart + vignette.substring(lastEnd);
            timeSegments.put(nextStart, seg.strip());
        }
        return timeSegments;
    }


    public String getPhenopacketBasedQuerySegment(String presentationTimeDescription, String input) {
        List<PhenotypicFeature> pfeatures = getPhenotypicFeatures(input);
        if (pfeatures.isEmpty()) {
            return ""; // no features detected for this time period
        }
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
        StringBuilder sb = new StringBuilder();
        String capitalizedTimepoint;
        if (presentationTimeDescription.equalsIgnoreCase("Examination was notable for")) {
            presentationTimeDescription = "On examination";
        }
        if (presentationTimeDescription == null || presentationTimeDescription.length() < 2) {
            capitalizedTimepoint = "";
        } else {
            capitalizedTimepoint = presentationTimeDescription.substring(0, 1).toUpperCase() + presentationTimeDescription.substring(1);
        }

        sb.append(capitalizedTimepoint);
        boolean observedEmpty = true;
        if (! observed_terms.isEmpty()) {
            observedEmpty = false;
            if (capitalizedTimepoint.isEmpty()) {
                sb.append("The patient presented with ");
            } else if (capitalizedTimepoint.equalsIgnoreCase("Other medical history included")) {
                sb.append(" "); // this will output Other medical history included X, Y, and Z
            } else {
                sb.append(", the patient presented with ");
            }

            String observedSymptoms = getSymptomList(observed_terms);
            sb.append(observedSymptoms).append(" ");
        }
        if (! excluded_terms.isEmpty()) {
            String excludededSymptoms = getSymptomList(excluded_terms);
            if (observedEmpty) {
                sb.append(", the following signs and symptoms were excluded: ");
            } else {
                sb.append("The following signs and symptoms were excluded: ");
            }
            sb.append(excludededSymptoms).append(" ");
        }
        return sb.toString();
    }

    /**
     * @param symptoms a list of HPO labels, e.g., X and Y and Z
     * @return A string formatted as X, Y, and Z.
     */
    private String getSymptomList(Set<String> symptoms) {
        StringBuilder sb = new StringBuilder();
        String symList = String.join(", ", symptoms);
        int jj = symList.lastIndexOf(", ");
        if (jj > 0) {
            symList = symList.substring(0, jj) + ", and " + symList.substring(jj+2);
        }
        sb.append(symList).append(".");
        return sb.toString();
    }



    List<PhenotypicFeature> getPhenotypicFeatures(String input) {
        List<PhenotypicFeature> pflist = new ArrayList<>();
        Collection<MinedTerm> minedTerms = this.miner.mineTerms(input);
        for (var mt : minedTerms) {
            boolean hpoObserved = mt.isPresent();
            TermId tid = TermId.of(mt.getTermIdAsString());
            if (!OntologyAlgorithm.isSubclass(hpo, tid, PHENOTYPIC_ABNORMALITY_ROOT)) {
                continue;
            }
            Optional<String> labelOpt = hpo.getTermLabel(tid);
            if (labelOpt.isEmpty()) continue;
            String label = labelOpt.get();
            if (label.equalsIgnoreCase("Negativism")) {
                continue; // common false positive, Negative is a synonym for negativism
            }
            PhenotypicFeatureBuilder builder = PhenotypicFeatureBuilder.builder(tid.getValue(), labelOpt.get());
            if (!hpoObserved) {
                builder.excluded();
            }
            pflist.add(builder.build());

        }
        return pflist;
    }

}
