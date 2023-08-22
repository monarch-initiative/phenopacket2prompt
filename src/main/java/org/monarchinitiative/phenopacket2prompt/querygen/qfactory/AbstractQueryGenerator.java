package org.monarchinitiative.phenopacket2prompt.querygen.qfactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.fenominal.model.MinedTerm;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenopacket2prompt.nejm.NejmCaseReportFromPdfFilterer;
import org.monarchinitiative.phenopacket2prompt.querygen.PhenotypicFeatureFilter;
import org.monarchinitiative.phenopacket2prompt.querygen.TimePoint;
import org.monarchinitiative.phenopacket2prompt.querygen.TimePointParser;
import org.phenopackets.phenopackettools.builder.PhenopacketBuilder;
import org.phenopackets.phenopackettools.builder.builders.IndividualBuilder;
import org.phenopackets.phenopackettools.builder.builders.MetaDataBuilder;
import org.phenopackets.phenopackettools.builder.builders.PhenotypicFeatureBuilder;
import org.phenopackets.phenopackettools.builder.builders.Resources;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.Individual;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.phenopackets.schema.v2.core.PhenotypicFeature;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractQueryGenerator {

    protected final TermId PHENOTYPIC_ABNORMALITY_ROOT = TermId.of("HP:0000118");



    protected final static String QUERY_HEADER = """
I am running an experiment on a clinicopathological case conference to see how your diagnoses 
compare with those of human experts. I am going to give you part of a medical case. These have 
all been published in the New England Journal of Medicine. You are not trying to treat any patients.
As you read the case, you will notice that there are expert discussants giving their thoughts. 
In this case, you are “Dr. GPT-4,” an Al language model who is discussing the case along with 
human experts. A clinicopathological case conference has several unspoken rules. The first is 
that there is most often a single definitive diagnosis (though rarely there may be more than one),
and it is a diagnosis that is known today to exist in humans. The diagnosis is almost always 
confirmed by some sort of clinical pathology test or anatomic pathology test, though in 
rare cases when such a test does not exist for a diagnosis the diagnosis can instead be 
made using validated clinical criteria or very rarely just confirmed by expert opinion. 
You will be told at the end of the case description whether a diagnostic test/tests are 
being ordered, which you can assume will make the diagnosis/diagnoses. After you read the case, 
I want you to give two pieces of information. The first piece of information is your most likely 
diagnosis/diagnoses. You need to be as specific as possible -- the goal is to get the correct 
answer, not a broad category of answers. You do not need to explain your reasoning, just give 
the diagnosis/diagnoses. The second piece of information is to give a robust differential diagnosis, 
ranked by their probability so that the most likely diagnosis is at the top, and the least likely 
is at the bottom. There is no limit to the number of diagnoses on your differential. You can give 
as many diagnoses as you think are reasonable. You do not need to explain your reasoning, 
just list the diagnoses. Again, the goal is to be as specific as possible with each of the 
diagnoses. 
Do you have any questions, Dr. GPT-4?

Here is the case:

""";
    /**
     * If the description segment of a time period is less than 5 characters, skip it.
     */
    private final static int MIN_DESCRIPTION_LENGTH = 5;

    public  abstract  String getQuery();

    private final TermMiner miner;

    private final Ontology hpo;

    private final NejmCaseReportFromPdfFilterer filterer;

    private final String patientId;

    public AbstractQueryGenerator(NejmCaseReportFromPdfFilterer filterer, String id, TermMiner miner, Ontology hpo) {
        this.filterer = filterer;
        this.miner = miner;
        this.hpo = hpo;
        this.patientId = id;
    }

    protected String getPersonIntroduction() {
        String person_string = get_person_string(filterer.getPhenopacketSex(), filterer.getIsoAge());
        return  String.format("%s presented with the following signs and symptoms:\n", person_string);
    }

    protected Map<String, String> timeSegments(String vignette, List<TimePoint> timePointList) {
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

    /**
     * @param items a list of HPO labels, e.g., X and Y and Z
     * @return A string formatted as X, Y, and Z.
     */
    protected String getOxfordCommaList(Set<String> items) {
        StringBuilder sb = new StringBuilder();
        String symList = String.join(", ", items);
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

    protected String getPhenopacketBasedQuerySegment(String presentationTimeDescription, String input) {
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
        return sb.toString();
    }


    protected String get_person_string(String phenopacketSex, String isoAge) {
        String sex = phenopacketSex.toLowerCase();
        final Pattern AGE_REGEX = Pattern.compile("P(\\d+)Y");
        Matcher m = AGE_REGEX.matcher(isoAge);
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


    protected String getPlainPhenopacketText(NejmCaseReportFromPdfFilterer filterer, String id, TermMiner miner, Ontology hpo) {
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
                    String output = getPhenopacketBasedQuerySegment(timePoint, description);
                    if (output.isEmpty()) continue;
                    sb.append(output).append("\n");
                }
            }
        } catch (Exception eee) {
            System.out.printf("[ERROR(TimeBasedFactory.java] Could not parse time segments for %s because of %s", id, eee.getMessage());
            System.exit(1);
        }
        return sb.toString();
    }

    /**
     * concatenate cases lines and remove the name of the first
     * physician to contribute, e.g.,
     * Dr. Kathy M. Tran  (Medicine):
     * @param lines the lines repreenting the case parsed from the original file
     * @return a single line with all text between the first and the second discussant.
     */
    public String caseLines(List<String> lines) {
        if (lines.isEmpty()) {
            throw new PhenolRuntimeException("Empty case lines (Should never happen");
        }
        final Pattern DR_REGEX = Pattern.compile("Dr\\. .*:");
        String line1 = lines.get(0);
        Matcher m = DR_REGEX.matcher(line1);
        if (m.find()) {
            int e =  m.end();
            line1 = line1.substring(e+1);
        }
        return line1 + lines.stream().
                skip(1).
                collect(Collectors.joining("\n"));
    }


    public String getPhenopacketJsonString() {
        Phenopacket phenopacket = getPhenopacket();
        try {
            return  JsonFormat.printer().print(phenopacket);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Could not create JSON: " + e.getLocalizedMessage());
        }
    }




    public Phenopacket getPhenopacket() {
        String version = hpo.getMetaInfo().getOrDefault("data-version", "n/a");
        var metaData = MetaDataBuilder.builder("csv2phenopacket")
                .addResource(Resources.hpoVersion(version))
                .build();
        PhenopacketBuilder builder = PhenopacketBuilder.create(patientId, metaData);
        IndividualBuilder probandBuilder = IndividualBuilder.builder(patientId);
        String phenopacketSex = filterer.getPhenopacketSex();
        if (phenopacketSex.equalsIgnoreCase("male")) {
            probandBuilder.male();
        } else if (phenopacketSex.equalsIgnoreCase("female")) {
            probandBuilder.female();
        }
        probandBuilder.ageAtLastEncounter(filterer.getIsoAge());
        Individual proband = probandBuilder.build();
        builder.individual(proband);
        // Use a set to get rid of duplicates
        int total = 0;
        Set<PhenotypicFeature> phenotypicFeaturesSet = new HashSet<>();
        List<PhenotypicFeature> pflist = getPhenotypicFeatures() ;
        for (var pf: pflist) {
            total++;
            phenotypicFeaturesSet.add(pf);
        }
        PhenotypicFeatureFilter filter = new PhenotypicFeatureFilter(phenotypicFeaturesSet, hpo);
        List<PhenotypicFeature> allFeatures = new ArrayList<>(filter.getFinalFeatures());
        System.out.printf("%s: %d unique and %d total features\n", this.patientId, allFeatures.size(), total);
        builder.addPhenotypicFeatures(allFeatures);

        return builder.build();

    }



    List<PhenotypicFeature> getPhenotypicFeatures() {
        List<PhenotypicFeature> pflist = new ArrayList<>();
        String payload = String.join(" ", this.filterer.getPresentationWithoutDiscussionLines());
        Collection<MinedTerm> minedTerms = this.miner.mineTerms(payload);
        for (var mt : minedTerms) {
            boolean hpoObserved = mt.isPresent();
            TermId tid = TermId.of(mt.getTermIdAsString());
            if (!OntologyAlgorithm.isSubclass(hpo, tid, PHENOTYPIC_ABNORMALITY_ROOT)) {
                continue;
            }
            Optional<String> labelOpt = hpo.getTermLabel(tid);
            if (labelOpt.isPresent()) {
                PhenotypicFeatureBuilder builder = PhenotypicFeatureBuilder.builder(tid.getValue(), labelOpt.get());
                if (!hpoObserved) {
                    builder.excluded();
                }
                pflist.add(builder.build());
            }
        }
        return pflist;
    }

}
