package org.monarchinitiative.phenopacket2prompt.output.impl.czech;

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

public class PpktIndividualCzechTest extends PPKtIndividualBase {

    private static Stream<PPKtIndividualBase.TestIndividual> testGetIndividualDescription() {
        return Stream.of(
                new PPKtIndividualBase.TestIndividual("46 year old female, infantile onset",
                        female46yearsInfantileOnset(), new PPKtIndividualBase.TestOutcome.Ok("Probandka byla 46 letá žena. První projevy onemocnění se u probandky objevily v kojeneckém věku.")),
                new PPKtIndividualBase.TestIndividual("male 4 months, congenital onset",
                        male4monthsCongenitalOnset(), new PPKtIndividualBase.TestOutcome.Ok("Proband byl kojenec mužského pohlaví vo věku 4 měsíců. První projevy onemocnění se u probanda objevily v perinatálním období.")),
                new PPKtIndividualBase.TestIndividual("female, no onset",
                        femaleNoAge(), new PPKtIndividualBase.TestOutcome.Ok("Probandka byla žena. Nástup onemocnění nebyl specifikován.")),
                new PPKtIndividualBase.TestIndividual("female, no HPOs",
                        femaleNoHPOs(), new PPKtIndividualBase.TestOutcome.Error(() -> new PhenolRuntimeException("Nessuna anomalia fenotipica"))),
                new PPKtIndividualBase.TestIndividual("unknown sex, no 4yo",
                        unknownSex4YearsOnset(), new PPKtIndividualBase.TestOutcome.Ok("Proband byla osoba blíže neurčeného pohlaví a věku. První projevy onemocnění se u probanda objevily v detství."))
        );
    }

    private static Stream<TestIdvlHeShe> testGetPPKtSex() {
        return Stream.of(
                new TestIdvlHeShe("female",
                        PhenopacketSex.FEMALE, new TestOutcome.Ok("žena")),
                new TestIdvlHeShe("male",
                        PhenopacketSex.MALE, new TestOutcome.Ok("muž")),
                new TestIdvlHeShe("proband",
                        PhenopacketSex.UNKNOWN, new TestOutcome.Ok("osoba"))
        );
    }

    private static Stream<TestIdvlAtAge> testIndlAtAge() {
        return Stream.of(
                new TestIdvlAtAge("congenital",
                        congenital, new TestOutcome.Ok("Od narození")),
                new TestIdvlAtAge("infantile",
                        infantile, new TestOutcome.Ok("Ako novorozenec")),
                new TestIdvlAtAge("childhood age",
                        childhood, new TestOutcome.Ok("V dětství")),
                new TestIdvlAtAge("46 years old",
                        p46y, new TestOutcome.Ok("Ve věku 46 let"))
        );
    }

    @ParameterizedTest
    @MethodSource("testGetIndividualDescription")
    void testEvaluateExpression(PPKtIndividualBase.TestIndividual testCase) {
        PPKtIndividualInfoGenerator generator = new PpktIndividualCzech();
        PpktIndividual ppkti = testCase.ppktIndividual();
        switch (testCase.expectedOutcome()) {
            case PPKtIndividualBase.TestOutcome.Ok(String expectedResult) ->
                    assertEquals(expectedResult, generator.getIndividualDescription(ppkti),
                            "Incorrect evaluation for: " + testCase.description());
            case PPKtIndividualBase.TestOutcome.Error(Supplier<? extends RuntimeException> exceptionSupplier) ->
                    assertThrows(exceptionSupplier.get().getClass(),
                            () -> generator.getIndividualDescription(ppkti),
                            "Incorrect error handling for: " + testCase.description());
        }
    }

    @ParameterizedTest
    @MethodSource("testGetPPKtSex")
    void testPPKtSex(TestIdvlHeShe testCase) {
        PPKtIndividualInfoGenerator generator = new PpktIndividualCzech();
        switch (testCase.expectedOutcome()) {
            case TestOutcome.Ok(String expectedResult) ->
                    assertEquals(expectedResult, generator.heSheIndividual(testCase.ppktSex()));
            case TestOutcome.Error(Supplier<? extends RuntimeException> exceptionSupplier) ->
                    assertThrows(exceptionSupplier.get().getClass(),
                            () -> generator.heSheIndividual(testCase.ppktSex()),
                            "Incorrect error handling for: " + testCase.description());
        }
    }

    @ParameterizedTest
    @MethodSource("testIndlAtAge")
    void testPPKtSex(TestIdvlAtAge testCase) {
        PPKtIndividualInfoGenerator generator = new PpktIndividualCzech();
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