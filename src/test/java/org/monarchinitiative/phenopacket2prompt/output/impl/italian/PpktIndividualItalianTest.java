package org.monarchinitiative.phenopacket2prompt.output.impl.italian;

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

public class PpktIndividualItalianTest extends PPKtIndividualBase{



    private static Stream<TestIndividual> testGetIndividualDescription() {
        return Stream.of(
                new TestIndividual("46 year old female, infantile onset",
                        female46yearsInfantileOnset(), new TestOutcome.Ok("La paziente era una donna di 46 anni. L'inizio della malattia avvenne durante il periodo infantile.")),
                new TestIndividual("male 4 months, congenital onset",
                        male4monthsCongenitalOnset(), new TestOutcome.Ok("Il paziente era un infante maschio di 4 mesi. L'inizio della malattia avvenne alla nascita.")),
                new TestIndividual("female, no onset",
                        femaleNoAge(), new TestOutcome.Ok("La paziente era di sesso femminile e di età non specificata. Non venne indicata l'età dell'inizio della malattia.")),
                new TestIndividual("female, no HPOs",
                        femaleNoHPOs(), new TestOutcome.Error(() -> new PhenolRuntimeException("Nessuna anomalia fenotipica"))),
                new TestIndividual("unknown sex, no 4yo",
                        unknownSex4YearsOnset(),  new TestOutcome.Ok("Il paziente era di sesso e di età non specificati. L'inizio della malattia avvenne da bambino."))
        );
    }



    @ParameterizedTest
    @MethodSource("testGetIndividualDescription")
    void testEvaluateExpression(TestIndividual testCase) {
        PPKtIndividualInfoGenerator generator = new PpktIndividualItalian();
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
                        PhenopacketSex.FEMALE, new TestOutcome.Ok("lei")),
                new TestIdvlHeShe("male",
                        PhenopacketSex.MALE, new TestOutcome.Ok("lui")),
                new TestIdvlHeShe("proband",
                        PhenopacketSex.UNKNOWN, new TestOutcome.Ok("il soggetto"))
        );
    }

    @ParameterizedTest
    @MethodSource("testGetPPKtSex")
    void testPPKtSex(TestIdvlHeShe testCase) {
        PPKtIndividualInfoGenerator generator = new PpktIndividualItalian();
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
                        congenital, new TestOutcome.Ok("Alla nascita")),
                new TestIdvlAtAge("infantile",
                        infantile, new TestOutcome.Ok("Durante il periodo infantile")),
                new TestIdvlAtAge("childhood age",
                        childhood, new TestOutcome.Ok("Durante l'infanzia")),
                new TestIdvlAtAge("46 years old",
                        p46y, new TestOutcome.Ok("All'età di 46 anni"))
        );
    }


    @ParameterizedTest
    @MethodSource("testIndlAtAge")
    void testPPKtSex(TestIdvlAtAge testCase) {
        PPKtIndividualInfoGenerator generator = new PpktIndividualItalian();
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
