package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.monarchinitiative.phenopacket2prompt.model.Iso8601Age;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PhenopacketIndividualInformationGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualBase;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PpktIndividualEnglishTest extends PPKtIndividualBase{



    private static Stream<TestCase> provideExpressionsForEvaluate() {
        return Stream.of(
                new TestCase("46 year olf female, infantile onset",
                        female46yearsInfantileOnset(), new TestOutcome.Success("The proband was a 46-year old woman who presented as an infant with")),
                new TestCase("male 4 months, congenital onset",
                        male4monthsCongenitalOnset(), new TestOutcome.Success("The proband was a 4-month old male infant who presented at birth with"))
        );
    }




    @ParameterizedTest
    @MethodSource("provideExpressionsForEvaluate")
    void testEvaluateExpression(TestCase testCase) {
        PhenopacketIndividualInformationGenerator generator = new PpktIndividualEnglish();
        PpktIndividual ppkti = testCase.ppktIndividual();
        switch (testCase.expectedOutcome()) {
            case TestOutcome.Success(String expectedResult) ->
                    assertEquals(expectedResult, generator.getIndividualDescription(ppkti),
                            "Incorrect evaluation for: " + testCase.description());
            case TestOutcome.Failure(Supplier<? extends RuntimeException> exceptionSupplier) ->
                    assertThrows(exceptionSupplier.get().getClass(),
                            () -> generator.getIndividualDescription(ppkti),
                            "Incorrect error handling for: " + testCase.description());
        }
    }



    @Test
    public void test1() {
        PpktIndividual ppkti = PPKtIndividualBase.female46yearsInfantileOnset();
        PhenopacketIndividualInformationGenerator generator = new PpktIndividualEnglish();
        String expected = "she";
        assertEquals(expected, generator.heSheIndividual(ppkti.getSex()));
        String expectedDescription = "The proband was a 46-year old woman who presented as an infant with";
        assertEquals(expectedDescription, generator.getIndividualDescription(ppkti));
        String expectedAtAge = "3";

    }

    @Test
    public void testIsoAge() {
        PhenopacketAge age = new Iso8601Age("P46Y");
        PhenopacketIndividualInformationGenerator generator = new PpktIndividualEnglish();
        assertEquals("P46Y", generator.atAge(age));
    }


}
