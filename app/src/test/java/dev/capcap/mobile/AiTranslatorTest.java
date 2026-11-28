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

    @Test public void partialResultKeepsUntranslatedCuesAndTimestamps() {
        java.util.List<Srt.Cue> source = Srt.parse(
                "1\n00:00:01,000 --> 00:00:02,000\nHello\n\n"
                + "2\n00:00:02,000 --> 00:00:03,000\nGoodbye");
        String partial = AiTranslator.buildPartialSrt(source, Arrays.asList("Xin chào"));
        java.util.List<Srt.Cue> restored = Srt.parse(partial);
        assertEquals(2, restored.size());
        assertEquals("Xin chào", restored.get(0).text);
        assertEquals("Goodbye", restored.get(1).text);
        assertEquals(2000L, restored.get(1).start);
    }

    @Test public void validatesModelNames() {
        assertTrue(AiTranslator.validModelName("gpt-5-mini"));
        assertTrue(AiTranslator.validModelName("gemini-2.5-flash"));
        assertFalse(AiTranslator.validModelName("../model"));
        assertFalse(AiTranslator.validModelName(""));
        assertFalse(AiTranslator.validModelName(null));
    }
}
