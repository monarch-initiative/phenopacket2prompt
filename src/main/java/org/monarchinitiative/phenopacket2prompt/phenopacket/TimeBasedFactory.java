package org.monarchinitiative.phenopacket2prompt.phenopacket;

import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.fenominal.model.MinedTerm;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenopacket2prompt.llm.ChatGptFilterer;
import org.phenopackets.phenopackettools.builder.builders.PhenotypicFeatureBuilder;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.Individual;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.phenopackets.schema.v2.core.PhenotypicFeature;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeBasedFactory extends QueryFactory {

    /**
     * If the description segment of a time period is less than 5 characters, skip it.
     */
    private final static int MIN_DESCRIPTION_LENGTH = 5;
    private final ChatGptFilterer filterer;

    private final TermMiner miner;

    private final Ontology hpo;

    private final String caseId;

    private final String isoAge;

    private final String phenopacketSex;

    private final String person_string;

    private final List<String> timePoints;
    public TimeBasedFactory(ChatGptFilterer filterer, String id, TermMiner miner, Ontology hpo, List<String> timePoints) {
        this.filterer = filterer;
        this.miner = miner;
        this.hpo = hpo;
        this.timePoints = timePoints;
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
        List<String> lines = filterer.getPresentationWithoutDiscussionLines();
        String vignette = String.join(" ", lines);
        int ii = vignette.indexOf(".");
        if (ii < 0) {
            throw new PhenolRuntimeException("Malformed vignette without one single period");
        }
        String firstSentence = vignette.substring(0, ii+1).strip();
        vignette = vignette.substring(ii+1);
        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        Map<Integer, String> start2pointMap = new HashMap<>();

        for (var point : timePoints) {
            int i = vignette.indexOf(point);
            if (i < 0) {
                System.out.printf("[ERROR] Could not find %s in vignette\n", point);
                System.exit(1); // should never happen if so die early
            }
            int j = i + point.length() - 1;
            starts.add(i);
            ends.add(j);
            start2pointMap.put(i, point);
        }
        Map<String, String> timeSegments = new TreeMap<>(); // ordered map
        String nextStart = "";
        int lastEnd = 0;
        for (int i = 0; i <starts.size(); i++) {
            int s = starts.get(i);
            int e = ends.get(i);
            String seg = nextStart + vignette.substring(lastEnd, s);
            lastEnd = e + 1;
            timeSegments.put(nextStart, seg.strip());
            nextStart = start2pointMap.get(s);
        }
        if (lastEnd < vignette.length()) {
            String seg = nextStart + vignette.substring(lastEnd);
            timeSegments.put(nextStart, seg.strip());
        }
        StringBuilder sb = new StringBuilder();
        sb.append(QUERY_HEADER);
        sb.append(firstSentence).append("\n");

        for (var entry : timeSegments.entrySet()) {
            String timePoint = entry.getKey();
            String description = entry.getValue();
            if ( description.length() > MIN_DESCRIPTION_LENGTH) {
                String output = getPhenopacketBasedQuerySegment(timePoint, description);
                if (output.isEmpty()) continue;
                sb.append(output).append("\n");
            }

        }
        return sb.toString();
    }


    public String getPhenopacketBasedQuerySegment(String timePoint, String input) {
        List<PhenotypicFeature> pfeatures = getPhenotypicFeatures(input);
        if (pfeatures.isEmpty()) {
            return ""; // no features detected for this time period
        }
        List<String> observed_terms = pfeatures.stream()
                .filter(Predicate.not(PhenotypicFeature::getExcluded))
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getLabel)
                .toList();
        List<String> excluded_terms = pfeatures.stream()
                .filter(PhenotypicFeature::getExcluded)
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getLabel)
                .toList();
        StringBuilder sb = new StringBuilder();
        String capitalizedTimepoint;
        if (timePoint == null || timePoint.length() < 2) {
            capitalizedTimepoint = "";
        } else {
            capitalizedTimepoint = timePoint.substring(0, 1).toUpperCase() + timePoint.substring(1);
        }
        sb.append(capitalizedTimepoint);
        boolean observedEmpty = true;
        if (! observed_terms.isEmpty()) {
            observedEmpty = false;
            if (capitalizedTimepoint.isEmpty()) {
                sb.append("The patient presented with ");
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
    private String getSymptomList(List<String> symptoms) {
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
