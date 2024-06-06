package org.monarchinitiative.phenopacket2prompt.output.impl.dutch;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualBase;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;
import org.monarchinitiative.phenopacket2prompt.output.impl.english.EnglishPromptGenerator;
import org.monarchinitiative.phenopacket2prompt.output.impl.english.PpktIndividualEnglish;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualBase.female46yearsInfantileOnset;
import static org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualBase.male4monthsCongenitalOnset;

public class PpktPhenotypicFeatureDutchTest {




    private static Stream<PPKtIndividualBase.TestIndividual> testGetIndividualPhenotypicFeatures() {
        return Stream.of(
                new PPKtIndividualBase.TestIndividual("46 year old female, infantile onset",
                        female46yearsInfantileOnset(), new PPKtIndividualBase.TestOutcome.Ok("Cerebellar atrophy and Ataxia")),
                new PPKtIndividualBase.TestIndividual("male 4 months, congenital onset",
                        male4monthsCongenitalOnset(), new PPKtIndividualBase.TestOutcome.Ok("Postaxial polydactyly"))
        );
    }

    @ParameterizedTest
    @MethodSource("testGetIndividualPhenotypicFeatures")
    void testEvaluateExpression(PPKtIndividualBase.TestIndividual testCase) {
        PPKtIndividualInfoGenerator generator = new PpktIndividualEnglish();
        EnglishPromptGenerator gen = new EnglishPromptGenerator();
        PpktIndividual ppkti = testCase.ppktIndividual();
        switch (testCase.expectedOutcome()) {
            case PPKtIndividualBase.TestOutcome.Ok(String expectedResult) ->
                    assertEquals(expectedResult, gen.formatFeatures(ppkti.getPhenotypicFeaturesAtOnset()),
                            "Incorrect evaluation for: " + testCase.description());
            case PPKtIndividualBase.TestOutcome.Error(Supplier<? extends RuntimeException> exceptionSupplier) ->
                    assertThrows(exceptionSupplier.get().getClass(),
                            () -> generator.getIndividualDescription(ppkti),
                            "Incorrect error handling for: " + testCase.description());
        }
    }

}
