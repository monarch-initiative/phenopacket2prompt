package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenopacket2prompt.model.Iso8601Age;
import org.monarchinitiative.phenopacket2prompt.model.HpoOnsetAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.phenopackets.phenopackettools.builder.PhenopacketBuilder;
import org.phenopackets.phenopackettools.builder.builders.*;
import org.phenopackets.schema.v2.core.Disease;
import org.phenopackets.schema.v2.core.Individual;
import org.phenopackets.schema.v2.core.MetaData;
import org.phenopackets.schema.v2.core.PhenotypicFeature;

import java.util.List;
import java.util.function.Supplier;

public class PPKtIndividualBase {
    private final static MetaData metadata = MetaDataBuilder.builder("curator").build();


    private final static PhenotypicFeature atrophy = PhenotypicFeatureBuilder.builder("HP:0001272", "Cerebellar atrophy" ).infantileOnset().build();
    private final static PhenotypicFeature ataxia = PhenotypicFeatureBuilder.builder("HP:0001251", "Ataxia").infantileOnset().build();
    private final static PhenotypicFeature bradyphrenExcluded = PhenotypicFeatureBuilder.builder("HP:0031843", "Bradyphrenia").excluded().build();
    private final static PhenotypicFeature polydactyly = PhenotypicFeatureBuilder.builder("HP:0100259", "Postaxial polydactyly").congenitalOnset().build();
    private final static PhenotypicFeature hepatomegalyNoOnset = PhenotypicFeatureBuilder.builder("HP:0002240","Hepatomegaly").build();
    private final static PhenotypicFeature lymphopenia = PhenotypicFeatureBuilder.builder("HP:0001888","Lymphopenia").iso8601onset("P3D").build();
    private final static PhenotypicFeature pneumonia = PhenotypicFeatureBuilder.builder("HP:0002090","Pneumonia").iso8601onset("P3D").build();
    private final static PhenotypicFeature igA = PhenotypicFeatureBuilder.builder("HP:0002720","Decreased circulating IgA level").iso8601onset("P3D").build();
    private final static PhenotypicFeature igM = PhenotypicFeatureBuilder.builder("HP:0002850","Decreased circulating total IgM").iso8601onset("P2Y").build();


    public sealed interface TestOutcome {
        record Ok(String value) implements TestOutcome {}
        record Error(Supplier<? extends RuntimeException> exceptionSupplier) implements TestOutcome {}
    }

    public record TestIndividual(String description, PpktIndividual ppktIndividual, TestOutcome expectedOutcome) {}

    public record TestIdvlHeShe(String description, PhenopacketSex ppktSex, TestOutcome expectedOutcome) {}


    public record TestIdvlAtAge(String description, PhenopacketAge ppktAge, TestOutcome expectedOutcome) {}




    public static PpktIndividual female46yearsInfantileOnset() {
        PhenopacketBuilder builder = PhenopacketBuilder.create("id1", metadata);
        Disease d = DiseaseBuilder.builder("OMIM:100123", "test").onset(TimeElements.infantileOnset()).build();
        Individual subject = IndividualBuilder.builder("individual.1").female().ageAtLastEncounter("P46Y").build();
        builder.individual(subject).addDisease(d).addPhenotypicFeature(atrophy).addPhenotypicFeature(ataxia).addPhenotypicFeature(bradyphrenExcluded);
        return new PpktIndividual(builder.build());
    }

    public static PpktIndividual male4monthsCongenitalOnset() {
        PhenopacketBuilder builder = PhenopacketBuilder.create("id2", metadata);
        Disease d = DiseaseBuilder.builder("OMIM:100123", "test").onset(TimeElements.congenitalOnset()).build();
        Individual subject = IndividualBuilder.builder("individual.2").male().ageAtLastEncounter("P4M").build();
        builder.individual(subject).addDisease(d).addPhenotypicFeature(polydactyly);
        return new PpktIndividual(builder.build());
    }

    public static PpktIndividual femaleNoAge() {
        PhenopacketBuilder builder = PhenopacketBuilder.create("id3", metadata);
        Disease d = DiseaseBuilder.builder("OMIM:100123", "test").build();
        Individual subject = IndividualBuilder.builder("individual.3").female().build();
        builder.individual(subject).addDisease(d).addPhenotypicFeature(hepatomegalyNoOnset);
        return new PpktIndividual(builder.build());
    }

    /**
     * Invalid phenopacket because no HPO annotationsa
     * @return
     */
    public static PpktIndividual femaleNoHPOs() {
        PhenopacketBuilder builder = PhenopacketBuilder.create("id4", metadata);
        Disease d = DiseaseBuilder.builder("OMIM:100123", "test").build();
        Individual subject = IndividualBuilder.builder("individual.4").female().build();
        return new PpktIndividual(builder.build());
    }

    public static PpktIndividual unknownSex4YearsOnset() {
        PhenopacketBuilder builder = PhenopacketBuilder.create("id5", metadata);
        Disease d = DiseaseBuilder.builder("OMIM:100123", "test").onset(TimeElements.childhoodOnset()).build();
        Individual subject = IndividualBuilder.builder("individual.5").unknownSex().build();
        builder.individual(subject).addDisease(d).addPhenotypicFeature(hepatomegalyNoOnset);
        return new PpktIndividual(builder.build());
    }


/*


Der Proband war niño de 2 años, der sich im Alter von 3 Tagen mit den folgenden Symptomen vorgestellt hat:
Lymphopenia, Pneumonia und Severe combined immunodeficiency. im Alter von 1 Monate y 0 Tage, er presentó Decreased lymphocyte proliferation in response to mitogen, Decreased circulating IgA level und Decreased circulating total IgM.
 */

    public static PpktIndividual twoYears() {
        PhenopacketBuilder builder = PhenopacketBuilder.create("id6", metadata);
        Disease d = DiseaseBuilder.builder("OMIM:100123", "test").onset(TimeElements.age("P3D")).build();
        Individual subject = IndividualBuilder.builder("individual.6").male().ageAtLastEncounter("P2Y").build();
        var features = List.of(lymphopenia, pneumonia, igA, igM);
        builder.individual(subject).addDisease(d).addPhenotypicFeatures(features);
        return new PpktIndividual(builder.build());
    }

    public static PpktIndividual PMID_9312167_A() {
        PhenopacketBuilder builder = PhenopacketBuilder.create("PMID_9312167_A:I:2", metadata);
        Disease d = DiseaseBuilder.builder("OMIM:179800", "Distal renal tubular acidosis 1").build();
        Individual subject = IndividualBuilder.builder("A:I:2").female().ageAtLastEncounter("P40Y").build();
        var pf1 = PhenotypicFeatureBuilder.builder("HP:0000121","Nephrocalcinosis").build();
        var pf2 = PhenotypicFeatureBuilder.builder("HP:0002900","Hypokalemia").build();
        var pf3 = PhenotypicFeatureBuilder.builder("HP:0032944","Alkaline urine").build();
        var pf4 = PhenotypicFeatureBuilder.builder("HP:0012100","Abnormal circulating creatinine concentration").excluded().build();
        var pf5 = PhenotypicFeatureBuilder.builder("HP:0008341","Distal renal tubular acidosis").excluded().build();
        var lst = List.of(pf1, pf2, pf3, pf4, pf5);
        builder.individual(subject).addDisease(d).addPhenotypicFeatures(lst);
        return new PpktIndividual(builder.build());
    }

    /*
    El paciente era un hombre de 30 años qui se presentó con se descartaron Máculas hipomelanóticas, Rabdomioma cardíaco y Bradicardia.A la edad de 30 años, el presentó Fositas o muescas (pits) del esmalte dental y Fibromatosis gingival.
     */
    public static PpktIndividual onlyExcludedAtPresentation() {
        PhenopacketBuilder builder = PhenopacketBuilder.create("id8", metadata);
        Disease d = DiseaseBuilder.builder("OMIM:100123", "test").onset(TimeElements.age("P3D")).build();
        Individual subject = IndividualBuilder.builder("individual.6").male().ageAtLastEncounter("P30Y").build();
        //  HP:Gingival fibromatosis HP:
        var pf1  = PhenotypicFeatureBuilder.builder("HP:0001662","Bradycardia").excluded().build();
        var pf2  = PhenotypicFeatureBuilder.builder("HP:0009729","Cardiac rhabdomyoma").excluded().build();
        var pf3  = PhenotypicFeatureBuilder.builder("HP:0009719","Hypomelanotic macule").excluded().build();
        var pf4 =PhenotypicFeatureBuilder.builder("HP:0009722","Dental enamel pits").onset(TimeElements.age("P30Y")).build();
        var pf5 =PhenotypicFeatureBuilder.builder("HP:0000169","Gingival fibromatosis").onset(TimeElements.age("P30Y")).build();
        var features = List.of(pf1,pf2,pf3,pf4,pf5);
        builder.individual(subject).addDisease(d).addPhenotypicFeatures(features);
        return new PpktIndividual(builder.build());
    }



    public static PhenopacketAge congenital = HpoOnsetAge.congenital();
    public static PhenopacketAge infantile = HpoOnsetAge.infantile();
    public static PhenopacketAge juvenile = HpoOnsetAge.juvenile();
    public static PhenopacketAge childhood = HpoOnsetAge.childhood();
    public static PhenopacketAge p46y =  new Iso8601Age("P46Y");





}
