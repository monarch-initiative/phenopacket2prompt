package org.monarchinitiative.phenopacket2prompt.international;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HpInternationalOboParserTest {
    /*private static final String pbpoPath = "data/small-hp-international.obo";
    private static final ClassLoader classLoader = HpInternationalOboParserTest.class.getClassLoader();
    private static final URL resource = (classLoader.getResource(pbpoPath));
    private static final File file = new File(resource.getFile());
    private static final HpInternationalOboParser parser = new HpInternationalOboParser(file);



    @Test
    public void testParser() {
        assertNotNull(parser);
    } */

    @Test
    public void testSpanish() {
        String line = "name: Displasia renal multiquística {babelon:source_value=\"Multicystic kidney dysplasia\", babelon:translation_status=\"OFFICIAL\", babelon:source_language=\"en\", babelon:translation_language=\"es\"}";
        Optional<String> opt = HpInternationalOboParser.getLanguage(line);
        assertTrue(opt.isPresent());
        String lang = opt.get();
        assertEquals("es", lang);
    }


    @Test
    public void testDutch() {
        String line = "babelon:translation_status=\"CANDIDATE\", babelon:source_value=\"All\", babelon:source_language=\"en\", babelon:translation_language=\"nl\"}";
        Optional<String> opt = HpInternationalOboParser.getLanguage(line);
        assertTrue(opt.isPresent());
        String lang = opt.get();
        assertEquals("nl", lang);

    }

}
