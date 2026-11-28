package dev.capcap.mobile;

import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class AiTranslatorTest {
    @Test public void parsesKeysFromLinesAndSeparators() {
        assertEquals(Arrays.asList("key-one", "key-two", "key-three"),
                AiTranslator.parseKeys(" key-one\nkey-two; key-three, key-one "));
    }

    @Test public void emptyKeyInputReturnsEmptyList() {
        assertTrue(AiTranslator.parseKeys("  \n ; , ").isEmpty());
        assertTrue(AiTranslator.parseKeys(null).isEmpty());
    }

    @Test public void validatesModelNames() {
        assertTrue(AiTranslator.validModelName("gpt-5-mini"));
        assertTrue(AiTranslator.validModelName("gemini-2.5-flash"));
        assertFalse(AiTranslator.validModelName("../model"));
        assertFalse(AiTranslator.validModelName(""));
        assertFalse(AiTranslator.validModelName(null));
    }
}
