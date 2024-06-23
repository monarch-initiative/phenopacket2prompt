package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.phenopacket2prompt.model.Iso8601Age;
import org.monarchinitiative.phenopacket2prompt.output.BuildingBlockGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PPKtEnglishBuildingBlocksTest {

    public final static BuildingBlockGenerator generator = new EnglishBuildingBlocks();

    @Test
    public void oneMonth() {
        String expected = "1 month";
        Iso8601Age iso = new Iso8601Age("P1M");
        String result = generator.fromIso(iso);
        assertEquals(expected, result);
    }


}
