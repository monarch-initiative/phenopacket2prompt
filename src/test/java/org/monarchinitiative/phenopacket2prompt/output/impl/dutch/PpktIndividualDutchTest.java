package org.monarchinitiative.phenopacket2prompt.output.impl.dutch;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualBase;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;
import org.monarchinitiative.phenopacket2prompt.output.impl.english.PpktIndividualEnglish;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PpktIndividualDutchTest extends PPKtIndividualBase{



    private static Stream<TestIndividual> testGetIndividualDescription() {
        return Stream.of(
                new TestIndividual("46 year olf female, infantile onset",
                        female46yearsInfantileOnset(), new TestOutcome.Ok("De proband was een 46-jaar oude vrouw die op zich op infantiele leeftijd presenteerde met")),
                new TestIndividual("male 4 months, congenital onset",
                        male4monthsCongenitalOnset(), new TestOutcome.Ok("De proband was een 4-maand oude mannelijk kind die zich bij de geboorte presenteerde met")),
                new TestIndividual("female, no onset",
                        femaleNoAge(), new TestOutcome.Ok("De proband was een vrouw die zich presenteerde met")),
                new TestIndividual("female, no HPOs",
                        femaleNoHPOs(), new TestOutcome.Error(() -> new PhenolRuntimeException("No HPO annotations"))),
                new TestIndividual("unknown sex, no 4mo",
                        unknownSex4MonthOnset(),  new TestOutcome.Ok("De proband presenteerde zich in de jeugd met"))
        );
    }



    @ParameterizedTest
    @MethodSource("testGetIndividualDescription")
    void testEvaluateExpression(TestIndividual testCase) {
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
                        PhenopacketSex.FEMALE, new TestOutcome.Ok("zij")),
                new TestIdvlHeShe("male",
                        PhenopacketSex.MALE, new TestOutcome.Ok("hij")),
                new TestIdvlHeShe("proband",
                        PhenopacketSex.UNKNOWN, new TestOutcome.Ok("het individu"))
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
                        congenital, new TestOutcome.Ok("Bij de geboorte")),
                new TestIdvlAtAge("infantile",
                        infantile, new TestOutcome.Ok("Tijdens de infantiele periode")),
                new TestIdvlAtAge("childhood age",
                        childhood, new TestOutcome.Ok("Tijdens de jeugd")),
                new TestIdvlAtAge("46 years old",
                        p46y, new TestOutcome.Ok("Op de leeftijd van 46 jaar"))
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
