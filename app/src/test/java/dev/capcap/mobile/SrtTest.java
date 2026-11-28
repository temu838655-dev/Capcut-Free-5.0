package dev.capcap.mobile;

import org.junit.Test;
import static org.junit.Assert.*;

public class SrtTest {
    @Test public void timing() {
        java.util.List<Srt.Cue> cues = Srt.parse(
                "1\r\n00:00:01,000 --> 00:00:02,000\r\nXin chào\r\n\r\n"
                + "2\r\n00:00:02,000 --> 00:00:03,000\r\nTạm biệt");
        assertEquals("", Srt.at(cues, 999));
        assertEquals("Xin chào", Srt.at(cues, 1000));
        assertEquals("Tạm biệt", Srt.at(cues, 2000));
        assertEquals("", Srt.at(cues, 3000));
    }

    @Test(expected = IllegalArgumentException.class)
    public void reversed() {
        Srt.parse("1\n00:00:02,000 --> 00:00:01,000\nInvalid");
    }

    @Test public void empty() {
        assertTrue(Srt.parse("").isEmpty());
        assertTrue(Srt.parse("  \r\n  ").isEmpty());
    }

    @Test public void whitespaceSeparators() {
        java.util.List<Srt.Cue> cues = Srt.parse(
                "1\n00:00:01,000 --> 00:00:02,000\nXin chào\n \n\t\n"
                + "2\n00:00:02,000 --> 00:00:03,000\nTạm biệt");
        assertEquals(2, cues.size());
        assertEquals("Xin chào", Srt.at(cues, 1500));
        assertEquals("Tạm biệt", Srt.at(cues, 2500));
    }

    @Test public void parsesBomCrLfAndMultilineSrtText() {
        java.util.List<Srt.Cue> cues = Srt.parse(
                "\uFEFF1\r\n00:00:01,250 --> 00:00:03,500\r\nDòng một\r\nDòng hai");
        assertEquals(1, cues.size());
        assertEquals(1250L, cues.get(0).start);
        assertEquals(3500L, cues.get(0).end);
        assertEquals("Dòng một\nDòng hai", cues.get(0).text);
    }

    @Test public void sortsOutOfOrderCues() {
        java.util.List<Srt.Cue> cues = Srt.parse(
                "1\n00:00:04,000 --> 00:00:05,000\nSau\n\n"
                + "2\n00:00:01,000 --> 00:00:02,000\nTrước");
        assertEquals("Trước", cues.get(0).text);
        assertEquals("Sau", cues.get(1).text);
    }

    @Test public void webVttWithIdentifierSettingsAndNotes() {
        java.util.List<Srt.Cue> cues = Srt.parse(
                "WEBVTT\n\nNOTE comment\nnot a cue\n\n"
                + "intro\n00:01.250 --> 00:03.500 align:start position:10%\n"
                + "Xin chào\nDòng hai\n\n00:04.000 --> 00:05.000\nTạm biệt");
        assertEquals(2, cues.size());
        assertEquals(1250L, cues.get(0).start);
        assertEquals(3500L, cues.get(0).end);
        assertEquals("Xin chào\nDòng hai", cues.get(0).text);
        assertEquals("Tạm biệt", cues.get(1).text);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsReversedVtt() {
        Srt.parse("WEBVTT\n\n00:03.000 --> 00:01.000\nInvalid");
    }

    @Test public void assDialoguePreservesCommasAndLineBreaks() {
        java.util.List<Srt.Cue> cues = Srt.parse(
                "[Script Info]\nTitle: Test\n[Events]\n"
                + "Format: Layer, Start, End, Style, Text\n"
                + "Dialogue: 0,0:00:01.25,0:00:03.50,Default,"
                + "{\\i1}Xin chào, bạn\\NDòng hai");
        assertEquals(1, cues.size());
        assertEquals(1250L, cues.get(0).start);
        assertEquals(3500L, cues.get(0).end);
        assertEquals("Xin chào, bạn\nDòng hai", cues.get(0).text);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidMinuteAndSecondFields() {
        Srt.parse("1\n00:61:00,000 --> 00:62:00,000\nInvalid");
    }
}
