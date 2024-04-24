package org.monarchinitiative.phenopacket2prompt.international;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HpInternationalOboParserTest {
    private static final String pbpoPath = "data/small-hp-international.obo";
    private static final ClassLoader classLoader = HpInternationalOboParserTest.class.getClassLoader();
    private static final URL resource = (classLoader.getResource(pbpoPath));
    private static final File file = new File(resource.getFile());
    private static final HpInternationalOboParser parser = new HpInternationalOboParser(file);

    @Test
    public void testParser() {
        assertNotNull(parser);
    }

    @Test
    public void testLanguage() {
        String line = "name: 手劈裂 {source:value=\"Split hand\", translation:status=\"OFFICIAL\", source:language=\"en\", translation:language=\"zh\"}";
        Optional<String> opt = parser.getLanguage(line);
        assertTrue(opt.isPresent());
        String lang = opt.get();
        assertEquals("zh", lang);
    }

}
