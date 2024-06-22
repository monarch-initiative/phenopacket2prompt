package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;

public interface PPKtIndividualInfoGenerator {

    String getIndividualDescription(PpktIndividual individual);

    String heSheIndividual(PhenopacketSex psex);

    /**
     * Generate an age description intended for the vignettes for a specified age (i.e., not for the very first sentence).
     * @param ppktAge
     * @return
     */
    String atAgeForVignette(PhenopacketAge ppktAge);

}
