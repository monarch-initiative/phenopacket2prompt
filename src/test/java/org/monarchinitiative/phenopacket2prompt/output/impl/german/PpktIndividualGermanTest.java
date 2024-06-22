package org.monarchinitiative.phenopacket2prompt.output.impl.german;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualBase;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PpktIndividualGermanTest extends PPKtIndividualBase{



    private static Stream<TestIndividual> testGetIndividualDescription() {
        return Stream.of(
                new TestIndividual("46 year old female, infantile onset",
                        female46yearsInfantileOnset(), new TestOutcome.Ok("Die Patientin war eine 46-jährige Frau, die sich im Säuglingsalter mit den folgenden Symptomen vorgestellt hat: ")),
              new TestIndividual("male 4 months, congenital onset",
                       male4monthsCongenitalOnset(), new TestOutcome.Ok("Der Patient war ein 4 Monate alter Säugling, der sich bei der Geburt mit den folgenden Symptomen vorgestellt hat: ")),
                  new TestIndividual("female, no onset",
                        femaleNoAge(), new TestOutcome.Ok("Die Patientin stellte sich mit den folgenden Symptomen vor: ")),
              new TestIndividual("female, no HPOs",
                        femaleNoHPOs(), new TestOutcome.Error(() -> new PhenolRuntimeException("No HPO annotations"))),
                new TestIndividual("unknown sex, no 4yo",
                        unknownSex4YearsOnset(),  new TestOutcome.Ok("Der Patient stellte sich in der Kindheit mit den folgenden Symptomen vor: "))
                         );
    }



    @ParameterizedTest
    @MethodSource("testGetIndividualDescription")
    void testEvaluateExpression(TestIndividual testCase) {
        PPKtIndividualInfoGenerator generator = new PpktIndividualGerman();
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
                        PhenopacketSex.FEMALE, new TestOutcome.Ok("sie")),
                new TestIdvlHeShe("male",
                        PhenopacketSex.MALE, new TestOutcome.Ok("er")),
                new TestIdvlHeShe("proband",
                        PhenopacketSex.UNKNOWN, new TestOutcome.Ok("die Person"))
        );
    }

    @ParameterizedTest
    @MethodSource("testGetPPKtSex")
    void testPPKtSex(TestIdvlHeShe testCase) {
        PPKtIndividualInfoGenerator generator = new PpktIndividualGerman();
        switch (testCase.expectedOutcome()) {
            case TestOutcome.Ok(String expectedResult) ->
                assertEquals(expectedResult, generator.heSheIndividual(testCase.ppktSex()));
            case TestOutcome.Error(Supplier<? extends RuntimeException> exceptionSupplier) ->
                    assertThrows(exceptionSupplier.get().getClass(),
                            () -> generator.heSheIndividual(testCase.ppktSex()),
                            "Incorrect error handling for: " + testCase.description());
        }
    }




    private static Stream<TestIdvlAtAge> testIndlAtAge() {
        return Stream.of(
                new TestIdvlAtAge("congenital",
                        congenital, new TestOutcome.Ok("Zum Zeitpunkt der Geburt")),
                new TestIdvlAtAge("infantile",
                        infantile, new TestOutcome.Ok("Als Säugling")),
                new TestIdvlAtAge("childhood age",
                        childhood, new TestOutcome.Ok("In der Kindheit")),
                new TestIdvlAtAge("46 years old",
                        p46y, new TestOutcome.Ok("Im Alter von 46 Jahren"))
        );
    }


    @ParameterizedTest
    @MethodSource("testIndlAtAge")
    void testPPKtSex(TestIdvlAtAge testCase) {
        PPKtIndividualInfoGenerator generator = new PpktIndividualGerman();
        switch (testCase.expectedOutcome()) {
            case TestOutcome.Ok(String expectedResult) ->
                    assertEquals(expectedResult, generator.atAgeForVignette(testCase.ppktAge()));
            case TestOutcome.Error(Supplier<? extends RuntimeException> exceptionSupplier) ->
                    assertThrows(exceptionSupplier.get().getClass(),
                            () -> generator.atAgeForVignette(testCase.ppktAge()),
                            "Incorrect error handling for: " + testCase.description());
        }
    }




}
