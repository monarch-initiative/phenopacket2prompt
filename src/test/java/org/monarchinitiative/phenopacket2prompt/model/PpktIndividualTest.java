package org.monarchinitiative.phenopacket2prompt.model;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.monarchinitiative.phenopacket2prompt.model.ppkt.PpktIndividual;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PpktIndividualTest {


    private static PpktIndividual ppktIndividual;


    @BeforeAll
    public static void init() {
        String ppktPath = "/data/GCDH_test_ppkt.json";
       // ClassLoader classLoader = PpktIndividualTest.class.getClassLoader();
      //  File file = new File(Objects.requireNonNull(classLoader.getResource(ppktPath)).getFile());
        Path resourceDirectory = Paths.get("src","test","resources", "data", "GCDH_test_ppkt.json");
        File file = resourceDirectory.toFile();
        ppktIndividual = new PpktIndividual(file);
        System.out.println(ppktIndividual);
    }

    @Test
    public void testCTOR() {
        Path resourceDirectory = Paths.get("src","test","resources", "data", "GCDH_test_ppkt.json");
        File file = resourceDirectory.toFile();
        PpktIndividual i = new PpktIndividual(file);
        System.out.println(ppktIndividual);
        Assertions.assertNotNull(ppktIndividual);
    }


    @Test
    public void testPhenopacketId() {
        Path resourceDirectory = Paths.get("src","test","resources", "data", "GCDH_test_ppkt.json");
        File file = resourceDirectory.toFile();
        PpktIndividual i = new PpktIndividual(file);


        String expected = "PMID_27672653_Individual_1";
        assertEquals(expected, i.getPhenopacketId());
    }





}
