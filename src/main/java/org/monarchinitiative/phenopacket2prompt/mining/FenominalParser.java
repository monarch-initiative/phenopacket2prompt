package org.monarchinitiative.phenopacket2prompt.mining;

import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.fenominal.model.MinedSentence;
import org.monarchinitiative.fenominal.model.MinedTermWithMetadata;
import org.monarchinitiative.phenol.io.OntologyLoader;


import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.phenopackets.phenopackettools.builder.PhenopacketBuilder;
import org.phenopackets.phenopackettools.builder.builders.IndividualBuilder;
import org.phenopackets.phenopackettools.builder.builders.MetaDataBuilder;
import org.phenopackets.phenopackettools.builder.builders.PhenotypicFeatureBuilder;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.Individual;
import org.phenopackets.schema.v2.core.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class FenominalParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FenominalParser.class);
    private final TermMiner miner;
    protected final Ontology ontology;


    private final String pmid = "PMID:123";
    private final String title = "title";



    public FenominalParser(File hpoJsonFile,  boolean exact) {
        this.ontology = OntologyLoader.loadOntology(hpoJsonFile);
        if (exact) {
            this.miner = TermMiner.defaultNonFuzzyMapper(this.ontology);
        } else {
            this.miner = TermMiner.defaultFuzzyMapper(this.ontology);
        }
    }

    private final static MetaData metadata = MetaDataBuilder.builder("curator").build();


    private Collection<MinedSentence> getMappedSentences (String content) {
        return miner.mineSentences(content);
    }



    private List<SimpleTerm> parseHpoTerms(String text) {
        List<SimpleTerm> simpleTermList = new ArrayList<>();

        Collection<MinedSentence> mappedSentences = getMappedSentences(text);

        for (var mp : mappedSentences) {
            Collection<? extends MinedTermWithMetadata> minedTerms = mp.getMinedTerms();
            for (var mt : minedTerms) {
                TermId tid = mt.getTermId();

                var opt = ontology.getTermLabel(tid);
                if (opt.isEmpty()) {
                    // should never happen
                    System.err.println("[ERROR] Could not find label for " + tid.getValue());
                    continue;
                }
                String label = opt.get();
               simpleTermList.add(new SimpleTerm(tid.getValue(), label, (! mt.isPresent())));
            }
        }
        return simpleTermList;
    }


    private static final Set<String> MALE = Set.of("male", "man", "boy");
    private static final Set<String> FEMALE = Set.of("female", "woman", "girl");



    private String getSex(String content) {
        String[] sentences = content.split("\\.");
        for (var s: sentences) {
            String ls = s.toLowerCase();
            if (ls.contains("patient")||ls.contains("proband") ||ls.contains("individual")) {
                if (MALE.stream().anyMatch(ls::contains)) {
                    return "male";
                } else if (FEMALE.stream().anyMatch(ls::contains)) {
                    return "female";
                }
            }
        }
        return null;
    }


    private Phenopacket generatePhenopacket(List<SimpleTerm> simpleTermList , String sex) {
        PhenopacketBuilder builder = PhenopacketBuilder.create(this.pmid, metadata);
        Individual subject;
        if (sex != null && sex.equals("male")) {
            subject = IndividualBuilder.builder("individual").male().build();
        } else if (sex != null && sex.equals("female")) {
            subject = IndividualBuilder.builder("individual").female().build();
        } else {
            subject = IndividualBuilder.builder("individual").build();
        }
        builder.individual(subject);
        for (var st : simpleTermList) {
            PhenotypicFeatureBuilder pfb = PhenotypicFeatureBuilder.builder(st.tid(), st.label());
            if (st.excluded()) {
                pfb.excluded();
            }
            builder.addPhenotypicFeature(pfb.build());
        }
        return builder.build();

    }


    public Phenopacket parse(String content) {
            List<SimpleTerm> simpleTermList = parseHpoTerms(content);
            String optSex = getSex(content);
            return generatePhenopacket(simpleTermList, optSex);
    }


}
