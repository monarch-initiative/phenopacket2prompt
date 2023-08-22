package org.monarchinitiative.phenopacket2prompt.querygen;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeBasedFatoryTest {


    private final static String vignette = "clinic of another hospital. On examination, there was {{conjunctival injection:PHENOTYPE}} in " +
                "both eyes. The {{lungs were clear on auscultation:EXCLUDE:Abnormal breath sound}}, and the remainder of the physical " +
                "examination was reportedly normal. Testing of a nasopharyngeal specimen for " +
                "severe acute respiratory syndrome coronavirus 2 {{(SARS-CoV-2) RNA was negative:LABORATORY}}.";

    private final static Pattern pattern = Pattern.compile("\\{\\{([^}]*)}}");


    @Test
    public void testRegex() {
        Matcher m = pattern.matcher(vignette);
        while (m.find()) {
            System.out.println(m.group());
        }

    }


}
