package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualBase;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PpktIndividualEnglishTest extends PPKtIndividualBase{



    private static Stream<TestIdvlDescription> testGetIndividualDescription() {
        return Stream.of(
                new TestIdvlDescription("46 year olf female, infantile onset",
                        female46yearsInfantileOnset(), new TestOutcome.Ok("The proband was a 46-year old woman who presented as an infant with")),
                new TestIdvlDescription("male 4 months, congenital onset",
                        male4monthsCongenitalOnset(), new TestOutcome.Ok("The proband was a 4-month old male infant who presented at birth with"))
        );
    }



    @ParameterizedTest
    @MethodSource("testGetIndividualDescription")
    void testEvaluateExpression(TestIdvlDescription testCase) {
        PPKtIndividualInfoGenerator generator = new PpktIndividualEnglish();
        PpktIndividual ppkti = testCase.ppktIndividual();
        switch (testCase.expectedOutcome()) {
            case TestOutcome.Ok(String expectedResult) ->
                    assertEquals(expectedResult, generator.getIndividualDescription(ppkti),
                            "Incorrect evaluation for: " + testCase.description());
            case TestOutcome.Error(Supplier<? extends RuntimeException> exceptionSupplier) ->
                    assertThrows(exceptionSupplier.get().getClass(),
                            () -> generator.getIndividualDescription(ppkti),
                            "Incorrect error handling for: " + testCase.description());
        }
    }



    private static Stream<TestIdvlHeShe> testGetPPKtSex() {
        return Stream.of(
                new TestIdvlHeShe("female",
                        PhenopacketSex.FEMALE, new TestOutcome.Ok("she")),
                new TestIdvlHeShe("male",
                        PhenopacketSex.MALE, new TestOutcome.Ok("he")),
                new TestIdvlHeShe("proband",
                        PhenopacketSex.UNKNOWN, new TestOutcome.Ok("the individual"))
        );
    }

    @ParameterizedTest
    @MethodSource("testGetPPKtSex")
    void testPPKtSex(TestIdvlHeShe testCase) {
        PPKtIndividualInfoGenerator generator = new PpktIndividualEnglish();
        switch (testCase.expectedOutcome()) {
            case TestOutcome.Ok(String expectedResult) ->
                assertEquals(expectedResult, generator.heSheIndividual(testCase.ppktSex()));
            case TestOutcome.Error(Supplier<? extends RuntimeException> exceptionSupplier) ->
                    assertThrows(exceptionSupplier.get().getClass(),
                            () -> generator.heSheIndividual(testCase.ppktSex()),
                            "Incorrect error handling for: " + testCase.description());
        }
    }



//public record TestIdvlAtAge(String description, PhenopacketAge ppktAge, TestOutcome expectedOutcome) {}




    private static Stream<TestIdvlAtAge> testIndlAtAge() {
        return Stream.of(
                new TestIdvlAtAge("congenital",
                        congenital, new TestOutcome.Ok("At birth")),
                new TestIdvlAtAge("infantile",
                        infantile, new TestOutcome.Ok("During the infantile period")),
                new TestIdvlAtAge("childhood age",
                        childhood, new TestOutcome.Ok("During childhood")),
                new TestIdvlAtAge("46 years old",
                        p46y, new TestOutcome.Ok("At an age of 46 years"))
        );
    }


    @ParameterizedTest
    @MethodSource("testIndlAtAge")
    void testPPKtSex(TestIdvlAtAge testCase) {
        PPKtIndividualInfoGenerator generator = new PpktIndividualEnglish();
        switch (testCase.expectedOutcome()) {
            case TestOutcome.Ok(String expectedResult) ->
                    assertEquals(expectedResult, generator.atAge(testCase.ppktAge()));
            case TestOutcome.Error(Supplier<? extends RuntimeException> exceptionSupplier) ->
                    assertThrows(exceptionSupplier.get().getClass(),
                            () -> generator.atAge(testCase.ppktAge()),
                            "Incorrect error handling for: " + testCase.description());
        }


    }






}
