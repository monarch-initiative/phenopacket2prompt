package org.monarchinitiative.phenopacket2prompt.cmd;


import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenopacket2prompt.mining.FenominalParser;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;
import org.phenopackets.phenopackettools.builder.PhenopacketBuilder;
import org.phenopackets.phenopackettools.builder.builders.*;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * This class creates simulated phenopackets using a number of variants so that we can see the effects on
 * our translations.
 */

@CommandLine.Command(name = "testdrive",
        mixinStandardHelpOptions = true,
        description = "Create varied prompts from simulated data and create a file for manual review")
public class TestDriveCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDriveCommand.class);

    @CommandLine.Option(names = {"--hp"},
            description = "path to HP json file")
    private String hpoJsonPath = "data/hp.json";

    @CommandLine.Option(names = {"--translations"},
            description = "path to translations file")
    private String translationsPath = "data/hp-international.obo";

    @CommandLine.Option(names = {"-o", "--outfile"},
            description = "outfile name (default {DEFAULT-VALUE})")
    private String outfileName = "p2p_test.txt";





    private final static Map<TermId, String> observedMap;
    private final static Map<TermId, String> excludedMap;
    private final static Map<TermId, String> observedSecondTimePoint;
    private final static Map<TermId, String> excludedSecondTimePoint;




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
        // for second time point
        observedSecondTimePoint = new HashMap<>();
        observedSecondTimePoint.put(TermId.of("HP:0003077"), "Hyperlipidemia");
        observedSecondTimePoint.put(TermId.of("HP:0003113"), "Hypochloremia");
        observedSecondTimePoint.put(TermId.of("HP:0002905"), "Hyperphosphatemia");
        observedSecondTimePoint.put(TermId.of("HP:0032097"), "Hypermanganesemia");
        observedSecondTimePoint.put(TermId.of("HP:0002901"), "Hypocalcemia");
        observedSecondTimePoint.put(TermId.of("HP:0004380"), "Aortic valve calcification");
        observedSecondTimePoint.put(TermId.of("HP:0004382"), "Mitral valve calcification");
        observedSecondTimePoint.put(TermId.of("HP:0001634"), "Mitral valve prolapse");
        observedSecondTimePoint.put(TermId.of("HP:0001712"), "Left ventricular hypertrophy");


        excludedSecondTimePoint= new HashMap<>();
        excludedSecondTimePoint.put(TermId.of("HP:0000400"), "Macrotia");
        excludedSecondTimePoint.put(TermId.of("HP:0400004"), "Long ear");
        excludedSecondTimePoint.put(TermId.of("HP:0000369"), "Low-set ears");
        excludedSecondTimePoint.put(TermId.of("HP:0012378"), "Fatigue");
        excludedSecondTimePoint.put(TermId.of("HP:0002664"), "Neoplasm");
    }

    private static PhenotypicFeature generatePF(TermId tid, String label, TimeElement telem, boolean excluded) {
        PhenotypicFeatureBuilder builder = PhenotypicFeatureBuilder.builder(tid.getValue(), label);
        if (telem != null) {
            builder.onset(telem);
        }
        if (excluded) {
            builder.excluded();
        }
        return builder.build();
    }

    private static PhenotypicFeature generatePF(TermId tid, String label, TimeElement telem) {
       return generatePF(tid, label, telem, false);
    }

    private static PhenotypicFeature generatePF(TermId tid, String label) {
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




    private static boolean randomChoice(double t) {
        double randomValue = RANDOM.nextDouble(); // returns double between 0 and 1
        return randomValue < t;
    }

    private static TimeElement getSecondObservedTime(TimeElement onset1) {
        if (onset1.hasOntologyClass()) {
            TermId firstHpo = TermId.of(onset1.getOntologyClass().getId());
            OntologyTerm second = getNextHpoOnset(firstHpo);
            return TimeElements.ontologyClass(OntologyClassBuilder.ontologyClass(second.getTid().getValue(), second.getLabel()));
        } else if (onset1.hasAge()) {
            String isoAge = onset1.getAge().getIso8601Duration();
            char digit = isoAge.charAt(1); // it#s always P3Y etc, i.e., the first character is always P and the next character is always the most significant age digit
            if (! Character.isDigit(digit)) {
                throw new PhenolRuntimeException("Malformed iso string " + isoAge);
            }
            int d =  Character.getNumericValue(digit);
            String nextIsoAge = String.format("P%d%s", ++d, isoAge.substring(2));
            return TimeElements.age(nextIsoAge);
        } else {
            // Not initialized, just return anything
            return TimeElements.age("P20Y2M");
        }
    }

    private final static List<OntologyTerm> orderedHpoOnsets;
    static  {
        orderedHpoOnsets = new ArrayList<>();
        List<TimeElement> clzList = List.of(TimeElements.antenatalOnset(),
              //  TimeElements.embryonalOnset(),
              //  TimeElements.fetalOnset(),
                TimeElements.congenitalOnset(),
                TimeElements.neonatalOnset(),
                TimeElements.infantileOnset(),
                TimeElements.childhoodOnset(),
                TimeElements.juvenileOnset(),
                TimeElements.youngAdultOnset(),
                TimeElements.middleAgeOnset(),
                TimeElements.lateOnset());
        for (TimeElement te: clzList) {
            OntologyClass clz = te.getOntologyClass();
            orderedHpoOnsets.add(new OntologyTerm(TermId.of(clz.getId()), clz.getLabel()));
        }
    }


    /**
     * Given an HPO Onset, return the next available onset at an older age.
     * @param firstHpo current age of onset, e.g., at initial presentation
     * @return another age of onset, at a secondary onset of an HPO term
     */
    private static OntologyTerm getNextHpoOnset(TermId firstHpo) {
        int N = orderedHpoOnsets.size();
        for (int i=0; i<N; i++) {
            if (orderedHpoOnsets.get(i).getTid().equals(firstHpo)) {
                if (i < N - 1) {
                    return orderedHpoOnsets.get(i + 1);
                }
            }
        }
        return orderedHpoOnsets.getLast();
    }


    /**
     * This method creates "random" phenopackets with various combinations of age, sex, observed and excluded terms,
     * and time points.
     * @return List of PpktIndividuals
     */
    private static List<PpktIndividual> getPpktIndividuals() {
        List<PpktIndividual> ppktIndividuals = new ArrayList<>();
        for (Disease d : diseaseList) {
            for (Individual i : individualList) {
                String randomId = generateRandomPassword(20);
                PhenopacketBuilder builder = PhenopacketBuilder.create(randomId, metadata);
                builder.individual(i).addDisease(d);
                // Add some terms at age of onset
                TimeElement onst = d.getOnset();
                TimeElement secondTime = getSecondObservedTime(onst);
                List<TermId> tidList = new ArrayList<>(observedMap.keySet());
                Collections.shuffle(tidList);
                int randomIndex = RANDOM.nextInt(tidList.size());
                if (randomChoice(0.8)) {
                    for (int ii = 0; ii < randomIndex; ii++) {
                        TermId tid = tidList.get(ii);
                        String label = observedMap.get(tid);
                        PhenotypicFeature pf = generatePF(tid, label, onst);
                        builder.addPhenotypicFeature(pf);
                    }
                } else {
                    // no onset
                    for (int ii = 0; ii < randomIndex; ii++) {
                        TermId tid = tidList.get(ii);
                        String label = observedMap.get(tid);
                        PhenotypicFeature pf = generatePF(tid, label);
                        builder.addPhenotypicFeature(pf);
                    }
                }
                // Add observed terms at second time point
                List<TermId> tidList2 = new ArrayList<>(observedSecondTimePoint.keySet());
                Collections.shuffle(tidList2);
                if (randomChoice(0.6)) {
                    randomIndex = RANDOM.nextInt(observedSecondTimePoint.size());
                    for (int ii = 0; ii < randomIndex; ii++) {
                        TermId tid = tidList2.get(ii);
                        String label = observedSecondTimePoint.get(tid);
                        PhenotypicFeature pf = generatePF(tid, label, secondTime);
                        builder.addPhenotypicFeature(pf);
                    }
                }
                // Add excluded terms at second time point
                tidList2 = new ArrayList<>(excludedSecondTimePoint.keySet());
                Collections.shuffle(tidList2);
                if (randomChoice(0.9)) {
                    randomIndex = RANDOM.nextInt(excludedSecondTimePoint.size());
                    for (int ii = 0; ii < randomIndex; ii++) {
                        TermId tid = tidList2.get(ii);
                        String label = excludedSecondTimePoint.get(tid);
                        PhenotypicFeature pf = generatePF(tid, label, secondTime);
                        builder.addPhenotypicFeature(pf);
                    }
                }
                tidList = new ArrayList<>(excludedMap.keySet());
                Collections.shuffle(tidList);
                randomIndex = RANDOM.nextInt(tidList.size());
                if (randomChoice(0.8)) {
                    for (int ii = 0; ii < randomIndex; ii++) {
                        TermId tid = tidList.get(ii);
                        String label = excludedMap.get(tid);
                        PhenotypicFeature pf = generatePF(tid, label, onst, true);
                        builder.addPhenotypicFeature(pf);
                    }
                } else {
                    // no onset
                    for (int ii = 0; ii < randomIndex; ii++) {
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
        }
        System.out.printf("[INFO] Added %d simulated individuals.\n", ppktIndividuals.size());
        return ppktIndividuals;
    }

    @Override
    public Integer call() {
        java.io.File hpJsonFile = new java.io.File(hpoJsonPath);
        boolean useExactMatching = true;
        if (! hpJsonFile.isFile()) {
            throw new PhenolRuntimeException("Could not find hp.json at " + hpJsonFile.getAbsolutePath());
        }
        Ontology hpo = OntologyLoader.loadOntology(hpJsonFile);
        LOGGER.info("HPO version {}", hpo.version().orElse("n/a"));
        FenominalParser parser = new FenominalParser(hpJsonFile, useExactMatching);
        java.io.File translationsFile = new java.io.File(translationsPath);
        if (! translationsFile.isFile()) {
            System.err.printf("Could not find translations file at %s. Try download command", translationsPath);
            return 1;
        }

        List<PpktIndividual> individualList = getPpktIndividuals();
        final String HEADER_LINE = "*******************************************\n\n";
        StringBuilder sb = new StringBuilder();

        String engText = createPrompts(individualList, PromptGenerator.english());
        sb.append("English\n");
        sb.append(HEADER_LINE);
        sb.append(engText);

        Utility utility = new Utility(translationsFile);
        PromptGenerator spanish = utility.spanish();
        String spText = createPrompts(individualList, spanish);
        sb.append("Spanish\n");
        sb.append(HEADER_LINE);
        sb.append(spText);
        String nlText = createPrompts(individualList, utility.dutch());
        sb.append("Dutch\n");
        sb.append(HEADER_LINE);
        sb.append(nlText);
        // GERMAN
        PromptGenerator german = utility.german();
        String deText = createPrompts(individualList, german);
        sb.append("German\n");
        sb.append(HEADER_LINE);
        sb.append(deText);
        String itText = createPrompts(individualList, utility.italian());
        sb.append("Italian\n");
        sb.append(HEADER_LINE);
        sb.append(itText);
        // ITALIAN
        String trText = createPrompts(individualList, utility.turkish());
        sb.append("Turkish\n");
        sb.append(HEADER_LINE);
        sb.append(trText);
        System.out.println(sb);
        System.out.println("Wrote to " + outfileName);
        try {
            Files.write(Paths.get(outfileName), sb.toString().getBytes());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }




        return 0;
    }


    private String createPrompts(List<PpktIndividual> individualList, PromptGenerator generator) {
        StringBuilder sb = new StringBuilder();
        for (PpktIndividual individual : individualList) {
            if (individual.hasExcludedPhenotypeFeatureAtOnset() ||individual.hasObservedPhenotypeFeatureAtOnset()) {
                String prompt = generator.createPromptWithoutHeader(individual);
                sb.append(prompt).append("\n\n");
            } else {
                System.err.println("[WARN] No HPO terms found for " + individual.getPhenopacketId());
            }
        }


        return sb.toString();
    }




}
