package org.monarchinitiative.phenopacket2prompt.phenopacket;


import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.monarchinitiative.phenopacket2prompt.llm.ChatGptFilterer;
import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.fenominal.model.MinedTerm;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
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

public class PhenopacketFactory {

    private final String isoAge;

    private final String phenopacketSex;
    private final ChatGptFilterer filterer;
    private final Ontology ontology;

    private final TermId PHENOTYPIC_ABNORMALITY_ROOT = TermId.of("HP:0000118");

    private final TermId NEGATIVISM = TermId.of("HP:0410291");
    private final TermMiner miner;

    private final String caseId;

    private final String diagnosis;

    private final static String QUERY_HEADER = """
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

    public String getDiagnosis() {
        return diagnosis;
    }

    public PhenopacketFactory(ChatGptFilterer filterer, String id, TermMiner tminer, Ontology ontology) {
        String age = filterer.getAge();
        String sex = filterer.getSex();
        this.phenopacketSex = filterer.getPhenopacketSex();
        this.isoAge = filterer.getIsoAge();
        this.filterer = filterer;
        this.miner = tminer;
        this.ontology = ontology;
        this.caseId = id;
        Optional<String> opt = filterer.getDiagnosis();
        diagnosis = opt.orElse("??? -- Could not be parsed");
    }



    public String getPhenopacketBasedQuery() {
        Phenopacket phenopacket = getPhenopacket();
        String person_string = get_person_string(phenopacket);
        List<String> observed_terms = phenopacket.getPhenotypicFeaturesList().stream()
                .filter(Predicate.not(PhenotypicFeature::getExcluded))
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getLabel)
                .toList();
        List<String> excluded_terms = phenopacket.getPhenotypicFeaturesList().stream()
                .filter(PhenotypicFeature::getExcluded)
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getLabel)
                .toList();
        StringBuilder sb = new StringBuilder();
        sb.append(QUERY_HEADER);
        sb.append(person_string);
        sb.append(" presented with the following signs and symptoms:\n");
        sb.append(String.join("\n", observed_terms)).append("\n\n");
        if (! excluded_terms.isEmpty()) {
            sb.append("The following signs and symptoms were excluded:\n");
            sb.append(String.join("\n", excluded_terms)).append("\n\n");
        }
        return sb.toString();
    }

    private String get_person_string(Phenopacket phenopacket) {
        Individual individual = phenopacket.getSubject();
        String sex = switch (individual.getSex()) {
            case MALE -> "male";
            case FEMALE -> "female";
            default -> "unknown";
        };
        String isoAge = individual.getTimeAtLastEncounter().getAge().getIso8601Duration();
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

    /**
     * concatenate cases lines and remove the name of the first
     * physician to contribute, e.g.,
     * Dr. Kathy M. Tran  (Medicine):
     * @param lines
     * @return
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


    public String getCaseTxt() {
        return QUERY_HEADER + caseLines(filterer.getCaseLines());
    }

    public String getCaseWithDifferentialTxt() {
        return QUERY_HEADER + caseLines(filterer.getAllLines());
    }

    public String getCasePriorToDiscussionTxt() {
        return  QUERY_HEADER + caseLines(filterer.getPresentationWithoutDiscussionLines());
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
        String version = ontology.getMetaInfo().getOrDefault("data-version", "n/a");
        var metaData = MetaDataBuilder.builder("csv2phenopacket")
                .addResource(Resources.hpoVersion(version))
                .build();
        PhenopacketBuilder builder = PhenopacketBuilder.create(caseId, metaData);
        IndividualBuilder probandBuilder = IndividualBuilder.builder(caseId);
        if (phenopacketSex.equalsIgnoreCase("male")) {
            probandBuilder.male();
        } else if (phenopacketSex.equalsIgnoreCase("female")) {
            probandBuilder.female();
        }
        probandBuilder.ageAtLastEncounter(this.isoAge);
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
        PhenotypicFeatureFilter filter = new PhenotypicFeatureFilter(phenotypicFeaturesSet, ontology);
        List<PhenotypicFeature> allFeatures = new ArrayList<>(filter.getFinalFeatures());
        System.out.printf("%s: %d unique and %d total features\n", this.caseId, allFeatures.size(), total);
        builder.addAllPhenotypicFeatures(allFeatures);

        return builder.build();

    }



    List<PhenotypicFeature> getPhenotypicFeatures() {
        List<PhenotypicFeature> pflist = new ArrayList<>();
        String payload = String.join(" ", this.filterer.getPresentationWithoutDiscussionLines());
        Collection<MinedTerm> minedTerms = this.miner.mineTerms(payload);
        for (var mt : minedTerms) {
            boolean hpoObserved = mt.isPresent();
            TermId tid = TermId.of(mt.getTermIdAsString());
            if (!OntologyAlgorithm.isSubclass(ontology, tid, PHENOTYPIC_ABNORMALITY_ROOT)) {
                continue;
            }
            Optional<String> labelOpt = ontology.getTermLabel(tid);
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
