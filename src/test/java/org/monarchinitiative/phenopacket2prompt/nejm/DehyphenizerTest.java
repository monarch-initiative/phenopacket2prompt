package org.monarchinitiative.phenopacket2prompt.nejm;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DehyphenizerTest {

    private static List<String> mylines;


    @BeforeAll
    public static void init() {
        mylines = new ArrayList<>();
        mylines.add("hospital. On examination, the pulse was 104 beats per minute and the blood pres-");
                mylines.add("sure 128/80 mm Hg. ");
        mylines.add("At the recommendation of his physicians, the patient stopped participating in all sports.");
    }


    @Test
    public void testRemoveHyphen() {
        List<String> cleanedLines = Dehyphenizer.dehyphenizeLines(mylines);
        String expectedLine1 = "hospital. On examination, the pulse was 104 beats per minute and the blood";
        String expectedLine2 = "pressure 128/80 mm Hg.";
        String expectedLine3 = "At the recommendation of his physicians, the patient stopped participating in all sports.";
        assertEquals(3, cleanedLines.size());
        assertEquals(expectedLine1, cleanedLines.get(0));
        assertEquals(expectedLine2, cleanedLines.get(1));
        assertEquals(expectedLine3, cleanedLines.get(2));
    }


}
