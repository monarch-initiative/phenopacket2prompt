package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenopacket2prompt.model.Iso8601Age;
import org.monarchinitiative.phenopacket2prompt.model.HpoOnsetAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.phenopackets.phenopackettools.builder.PhenopacketBuilder;
import org.phenopackets.phenopackettools.builder.builders.DiseaseBuilder;
import org.phenopackets.phenopackettools.builder.builders.IndividualBuilder;
import org.phenopackets.phenopackettools.builder.builders.MetaDataBuilder;
import org.phenopackets.phenopackettools.builder.builders.TimeElements;
import org.phenopackets.schema.v2.core.Disease;
import org.phenopackets.schema.v2.core.Individual;
import org.phenopackets.schema.v2.core.MetaData;

import java.util.function.Supplier;

public class PPKtIndividualBase {
    private final static MetaData metadata = MetaDataBuilder.builder("curator").build();

    public sealed interface TestOutcome {
        record Ok(String value) implements TestOutcome {}
        record Error(Supplier<? extends RuntimeException> exceptionSupplier) implements TestOutcome {}
    }

    public record TestIdvlDescription(String description, PpktIndividual ppktIndividual, TestOutcome expectedOutcome) {}

    public record TestIdvlHeShe(String description, PhenopacketSex ppktSex, TestOutcome expectedOutcome) {}


    public record TestIdvlAtAge(String description, PhenopacketAge ppktAge, TestOutcome expectedOutcome) {}




    public static PpktIndividual female46yearsInfantileOnset() {
        PhenopacketBuilder builder = PhenopacketBuilder.create("id1", metadata);
        Disease d = DiseaseBuilder.builder("OMIM:100123", "test").onset(TimeElements.infantileOnset()).build();
        Individual subject = IndividualBuilder.builder("individual.1").female().ageAtLastEncounter("P46Y").build();
        builder.individual(subject).addDisease(d);
        return new PpktIndividual(builder.build());
    }

    public static PpktIndividual male4monthsCongenitalOnset() {
        PhenopacketBuilder builder = PhenopacketBuilder.create("id2", metadata);
        Disease d = DiseaseBuilder.builder("OMIM:100123", "test").onset(TimeElements.congenitalOnset()).build();
        Individual subject = IndividualBuilder.builder("individual.2").male().ageAtLastEncounter("P4M").build();
        builder.individual(subject).addDisease(d);
        return new PpktIndividual(builder.build());
    }







    public static PhenopacketAge congenital = HpoOnsetAge.congenital();
    public static PhenopacketAge infantile = HpoOnsetAge.infantile();
    public static PhenopacketAge juvenile = HpoOnsetAge.juvenile();
    public static PhenopacketAge childhood = HpoOnsetAge.childhood();
    public static PhenopacketAge p46y =  new Iso8601Age("P46Y");





}
