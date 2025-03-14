package org.monarchinitiative.phenopacket2prompt.output.impl.czech;

import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;

import java.util.List;
import java.util.Set;

public class CzechPromptGenerator implements PromptGenerator {

    private final PPKtIndividualInfoGenerator individualInfoGenerator;
    private final PpktPhenotypicFeatureGenerator ppktPhenotypicFeatureGenerator;
    private final PhenopacketTextGenerator ppktTextGenerator;


    public CzechPromptGenerator(PpktPhenotypicFeatureGenerator pfgen) {
        individualInfoGenerator = new PpktIndividualCzech();
        ppktTextGenerator = new PpktTextCzech();
        ppktPhenotypicFeatureGenerator = pfgen;
    }

    @Override
    public String queryHeader() {
        return ppktTextGenerator.LLM_PROMPT_HEADER();
    }

    @Override
    public String queryFooter() {
        return ppktTextGenerator.LLM_PROMPT_FOOTER();
    }

    @Override
    public String getIndividualInformation(PpktIndividual ppktIndividual) {
        return individualInfoGenerator.getIndividualDescription(ppktIndividual);
    }

    @Override
    public String formatFeatures(List<OntologyTerm> ontologyTerms) {
        return ppktPhenotypicFeatureGenerator.formatFeatures(ontologyTerms);
    }

    @Override
    public String getVignetteAtAge(PhenopacketAge page, PhenopacketSex psex, List<OntologyTerm> terms) {
        String proband = switch (psex) {
            case FEMALE -> Nouns.PROBAND.nominativ(Genus.SHE);  // Probandka
            case MALE, OTHER, UNKNOWN -> Nouns.PROBAND.nominativ(Genus.HE);  // Proband
        };
        proband = proband.substring(0, 1).toUpperCase() + proband.substring(1);
        String sexAdjective = switch (psex) {
            case FEMALE -> Adjectives.FEMALE.genitiv(Genus.IT);  // sex is neutrum in Czech
            case MALE -> Adjectives.MALE.genitiv(Genus.IT); // sex is neutrum in Czech
            default -> "neuvedeného";  // "unspecified"
        };
        String ageString = this.individualInfoGenerator.atAgeForVignette(page);
        String features = formatFeatures(terms);
        String presented = switch (psex) {
            case MALE, OTHER, UNKNOWN -> "prezentoval";
            case FEMALE -> "prezentovala";
        };
        // Proband zenskeho/muzskeho pohlavia sa vo veku ... prezentoval s nasledujicimi symptomy
        return String.format(
                "%s %s pohlaví se vo věku %s %s s následujícími symptomy: %s",
                proband,
                sexAdjective,
                ageString,
                presented,
                features
        );
    }

    @Override
    public Set<String> getMissingTranslations() {
        return ppktPhenotypicFeatureGenerator.getMissingTranslations();
    }
}
