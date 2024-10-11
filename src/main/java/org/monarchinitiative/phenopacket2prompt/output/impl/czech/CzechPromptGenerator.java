package org.monarchinitiative.phenopacket2prompt.output.impl.czech;

import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;

import java.util.List;
import java.util.Set;

public class CzechPromptGenerator implements PromptGenerator {

    private final PPKtIndividualInfoGenerator individualInfoGenerator;
    private final PpktPhenotypicFeatureGenerator ppktPhenotypicFeatureGenerator;
    private final String header = """
Provádím experiment na kazuistice, abych jsem porovnal Vaše diagnózy s diagnózami od expertov. Odprezentujem vám část kazuistiky.
V tomto případě jste "Dr. GPT-4", jazykový model, který poskytuje diagnózu. Mám pro vás několik pokynů.
Za prvé, existuje jediná definitivní diagnóza a je to diagnóza o kterej existence je dnes známa u lidí.
Diagnóza je téměř vždy potvrzena nějakým druhem genetického testu, i když ve vzácných případech
pokud takový test pro diagnózu neexistuje, může být místo toho stanovena pomocí ověřených klinických kritérií
nebo velmi zřídka jen potvrzena znaleckým posudkem. Až si přečtete případ, chci, abyste provedl diferenciální diagnostiku
a poskytl seznam kandidátních diagnóz seřazených podle pravděpodobnosti počínaje nejpravděpodobnějším kandidátem.
Každá kandidátní diagnóza by měla být specifikována názvem choroby.
Například, pokud je prvním kandidátem Branchio-okulo-faciální syndrom a druhým je Cystická fibróza, uveďte následující v angličtině:

1. Branchiooculofacial syndrome
2. Cystic fibrosis

Tento seznam by měl obsahovat tolik diagnóz, kolik považujete za rozumné. Nemusíte zdůvodnit svoji volbu, stačí vypsat diagnózy.
Zde je kazuistika:

""";

    public CzechPromptGenerator(PpktPhenotypicFeatureGenerator pfgen) {
        individualInfoGenerator = new PpktIndividualCzech();
        ppktPhenotypicFeatureGenerator = pfgen;
    }

    @Override
    public String queryHeader() {
        return header;
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
            case FEMALE -> Adjectives.FEMALE.genitiv(Genus.SHE);
            case MALE -> Adjectives.MALE.genitiv(Genus.HE);
            default -> "neuvedeného";  // "unspecified"
        };
        String ageString = this.individualInfoGenerator.atAgeForVignette(page);
        String features = formatFeatures(terms);
        // Proband zenskeho/muzskeho pohlavia sa vo veku ... prezentoval s nasledujicimi symptomy
        return String.format(
                "%s %s pohlaví se vo věku %s prezentoval s následujícími symptomy: %s",
                proband,
                sexAdjective,
                ageString,
                features
        );
    }

    @Override
    public Set<String> getMissingTranslations() {
        return ppktPhenotypicFeatureGenerator.getMissingTranslations();
    }
}
