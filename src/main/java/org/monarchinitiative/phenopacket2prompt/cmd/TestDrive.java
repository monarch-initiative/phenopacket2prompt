package org.monarchinitiative.phenopacket2prompt.cmd;


import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.phenopackets.phenopackettools.builder.PhenopacketBuilder;
import org.phenopackets.phenopackettools.builder.builders.*;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.*;

import java.util.*;

/**
 * This class creates simulated phenopackets using a number of variants so that we can see the effects on
 * our translations.
 */
public class TestDrive {


    private final static Map<TermId, String> observedMap;
    private final static Map<TermId, String> excludedMap;


    private final static  Random RANDOM = new Random();

    static {
        observedMap = new HashMap<>();
        observedMap.put(TermId.of("HP:0001272"), "Cerebellar atrophy");
        observedMap.put(TermId.of("HP:0001251"), "Ataxia");
        observedMap.put(TermId.of("HP:0100259"), "Postaxial polydactyly");
        observedMap.put(TermId.of("HP:0002240"), "Hepatomegaly");
        observedMap.put(TermId.of("HP:0001888"), "Lymphopenia");
        observedMap.put(TermId.of("HP:0002090"), "Pneumonia");
        observedMap.put(TermId.of("HP:0002720"), "Decreased circulating IgA level");
        observedMap.put(TermId.of("HP:0002850"), "Decreased circulating total IgM");
        observedMap.put(TermId.of("HP:0001609"), "Hoarse voice");
        excludedMap = new HashMap<>();
        excludedMap.put(TermId.of("HP:0031843"), "Bradyphrenia");
        excludedMap.put(TermId.of("HP:0003228"), "Hypernatremia");
        excludedMap.put(TermId.of("HP:0002900"), "Hypokalemia");
        excludedMap.put(TermId.of("HP:0001629"), "Ventricular septal defect");
        excludedMap.put(TermId.of("HP:0000083"), "Renal insufficiency");
    }

    private PhenotypicFeature generatePF(TermId tid, String label, TimeElement telem, boolean excluded) {
        PhenotypicFeatureBuilder builder = PhenotypicFeatureBuilder.builder(tid.getValue(), label);
        if (telem != null) {
            builder.onset(telem);
        }
        if (excluded) {
            builder.excluded();
        }
        return builder.build();
    }

    private PhenotypicFeature generatePF(TermId tid, String label, TimeElement telem) {
       return generatePF(tid, label, telem, false);
    }

    private PhenotypicFeature generatePF(TermId tid, String label) {
        return generatePF(tid, label, null);
    }

    public static String generateRandomPassword(int len) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk"
                +"lmnopqrstuvwxyz!@#$%&";

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        return sb.toString();
    }




    private final static PhenotypicFeature e1 = PhenotypicFeatureBuilder.builder("", "").excluded().build();
    private final static PhenotypicFeature e2 = PhenotypicFeatureBuilder.builder("", "").excluded().build();
    private final static PhenotypicFeature e3 = PhenotypicFeatureBuilder.builder("", "").excluded().build();
    private final static PhenotypicFeature e4 = PhenotypicFeatureBuilder.builder("", "").excluded().build();
    private final static PhenotypicFeature e5 = PhenotypicFeatureBuilder.builder("HP:0001395", "Hepatic fibrosis").excluded().build();
    private final static List<PhenotypicFeature> excludedFeatureList = List.of(e1,e2, e3, e4, e5);


    private final static Disease d1 = DiseaseBuilder.builder("OMIM:162200", "Neurofibromatosis, type 1").build();
    private final static Disease d2 = DiseaseBuilder.builder("OMIM:613224", "Noonan syndrome 6").onset(TimeElements.antenatalOnset()).build();
    private final static Disease d3 = DiseaseBuilder.builder("OMIM:113620", "Branchiooculofacial syndrome").onset(TimeElements.congenitalOnset()).build();
    private final static Disease d4 = DiseaseBuilder.builder("OMIM:220150", "Hypouricemia, renal, 1").onset(TimeElements.childhoodOnset()).build();
    private final static Disease d5 = DiseaseBuilder.builder("OMIM:154700", "Marfan syndrome").onset(TimeElements.age("P12Y4M")).build();
    private final static Disease d6 = DiseaseBuilder.builder("OMIM:109150", "Machado-Joseph disease").onset(TimeElements.age("P42Y")).build();
    private final static Disease d7 = DiseaseBuilder.builder("OMIM:605275", "Noonan syndrome 2").onset(TimeElements.age("P2D")).build();

    private final static List<Disease> diseaseList = List.of(d1, d2, d3, d4, d5, d6, d7);

    private final static Individual s1 = IndividualBuilder.builder("individual.1").female().ageAtLastEncounter("P46Y").build();
    private final static Individual s2 = IndividualBuilder.builder("individual.2").male().ageAtLastEncounter("P46Y").build();
    private final static Individual s3 = IndividualBuilder.builder("individual.3").unknownSex().ageAtLastEncounter("P46Y").build();
    private final static Individual s4 = IndividualBuilder.builder("individual.4").female().ageAtLastEncounter(TimeElements.juvenileOnset()).build();
    private final static Individual s5 = IndividualBuilder.builder("individual.5").male().ageAtLastEncounter(TimeElements.middleAgeOnset()).build();
    private final static Individual s6 = IndividualBuilder.builder("individual.6").unknownSex().ageAtLastEncounter(TimeElements.adultOnset()).build();
    private final static Individual s7 = IndividualBuilder.builder("individual.7").female().build();
    private final static Individual s8 = IndividualBuilder.builder("individual.8").male().build();
    private final static Individual s9 = IndividualBuilder.builder("individual.9").unknownSex().build();

    private final static List<Individual> individualList = List.of(s1,s2,s3,s4,s5,s6,s7,s8,s9);

    private final static MetaData metadata = MetaDataBuilder.builder("curator").build();


    private final List<PpktIndividual> ppktIndividuals;


    private boolean randomChoice(double t) {
        double randomValue = RANDOM.nextDouble(); // returns double between 0 and 1
        return randomValue < t;
    }



    public TestDrive() {
        ppktIndividuals = new ArrayList<>();
        for (Disease d : diseaseList) {
            for (Individual i: individualList) {
                String randomId = generateRandomPassword(20);
                PhenopacketBuilder builder = PhenopacketBuilder.create(randomId, metadata);
                builder.individual(i).addDisease(d);
                // Add some terms at age of onset
                TimeElement onst = d.getOnset();
                List<TermId> tidList = new ArrayList<>(observedMap.keySet());
                Collections.shuffle(tidList);
                int randomIndex = RANDOM.nextInt(tidList.size());
                if (randomChoice(0.8)) {
                    for (int ii=0; ii<randomIndex;ii++) {
                        TermId tid = tidList.get(ii);
                        String label = observedMap.get(tid);
                        PhenotypicFeature pf = generatePF(tid, label, onst);
                        builder.addPhenotypicFeature(pf);
                    }
                } else {
                        // no onset
                    for (int ii=0; ii<randomIndex;ii++) {
                        TermId tid = tidList.get(ii);
                        String label = observedMap.get(tid);
                        PhenotypicFeature pf = generatePF(tid, label);
                        builder.addPhenotypicFeature(pf);
                    }
                }
                tidList = new ArrayList<>(excludedMap.keySet());
                Collections.shuffle(tidList);
                randomIndex = RANDOM.nextInt(tidList.size());
                if (randomChoice(0.8)) {
                    for (int ii=0; ii<randomIndex;ii++) {
                        TermId tid = tidList.get(ii);
                        String label = excludedMap.get(tid);
                        PhenotypicFeature pf = generatePF(tid, label, onst, true);
                        builder.addPhenotypicFeature(pf);
                    }
                } else {
                    // no onset
                    for (int ii=0; ii<randomIndex;ii++) {
                        TermId tid = tidList.get(ii);
                        String label = excludedMap.get(tid);
                        PhenotypicFeature pf = generatePF(tid, label, null, true);
                        builder.addPhenotypicFeature(pf);
                    }
                }
                Phenopacket ppkt = builder.build();
                PpktIndividual individual = new PpktIndividual(ppkt);
                ppktIndividuals.add(individual);
            }
            System.out.printf("[INFO] Added %d simulated individuals.\n", ppktIndividuals.size());
        }

    }









}
