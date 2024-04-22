package org.monarchinitiative.phenopacket2prompt.model;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenopacket2prompt.model.ppkt.*;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class PpktIndividualTest {



    private static final String ppktPath = "data/GCDH_test_ppkt.json";
    private static final ClassLoader classLoader = PpktIndividualTest.class.getClassLoader();
    private static final URL resource = (classLoader.getResource(ppktPath));
    private static final File file = new File(resource.getFile());
    private static final PpktIndividual ppktIndividual = new PpktIndividual(file);


    @Test
    public void testCTOR() {
        Assertions.assertNotNull(ppktIndividual);
    }


    @Test
    public void testPhenopacketId() {
        String expected = "PMID_27672653_Individual_1";
        assertEquals(expected, ppktIndividual.getPhenopacketId());
    }


    @Test
    public void testPhenopacketSex() {
        assertEquals(PhenopacketSex.MALE, ppktIndividual.getSex());
    }


    @Test
    public void testPhenopacketAgeLastEncounter() {
        Optional<PhenopacketAge> opt = ppktIndividual.getAgeAtLastExamination();
        assertTrue(opt.isPresent());
        PhenopacketAge ppktAge = opt.get();
        assertEquals(PhenopacketAgeType.ISO8601_AGE_TYPE, ppktAge.ageType());
        String iso = ppktAge.age();
        assertEquals("P20Y", iso);
    }

    @Test
    public void testPhenopacketOnset() {
        Optional<PhenopacketAge> opt = ppktIndividual.getAgeAtOnset();
        assertTrue(opt.isPresent());
        PhenopacketAge onsetAge = opt.get();
        assertEquals(PhenopacketAgeType.ISO8601_AGE_TYPE, onsetAge.ageType());
        String iso = onsetAge.age();
        assertEquals("P5M", iso);
    }

    @Test
    public void testPhenopacketDisease() {
        List<PhenopacketDisease> diseases = ppktIndividual.getDiseases();
        assertEquals(1, diseases.size());
        PhenopacketDisease disease = diseases.get(0);
        TermId expectedId = TermId.of("OMIM:231670");
        String expectedLabel =  "Glutaricaciduria, type I";
        assertEquals(expectedId, disease.getDiseaseId());
        assertEquals(expectedLabel, disease.getLabel());
    }

    @Test
    public void testPhenotypicFeatures() {
        List<OntologyTerm> ppktFeatures = ppktIndividual.getPhenotypicFeatures();
        assertFalse(ppktFeatures.isEmpty());
        Predicate<OntologyTerm> termPredicate = term -> term.getLabel().equals("Cerebral atrophy");
        Optional<OntologyTerm> opt = ppktFeatures.stream().filter(termPredicate).findAny();
        assertTrue(opt.isPresent());
        OntologyTerm term = opt.get();
        assertEquals("Cerebral atrophy", term.getLabel());
        TermId expectedId = TermId.of("HP:0002059");
        assertEquals(expectedId, term.getTid());
        assertTrue(term.isExcluded());
    }

}
