package dev.capcap.mobile;
import org.junit.Test;
import static org.junit.Assert.*;
public class SrtTest {
 @Test public void timing(){var c=Srt.parse("1\r\n00:00:01,000 --> 00:00:02,000\r\nXin chào\r\n\r\n2\r\n00:00:02,000 --> 00:00:03,000\r\nTạm biệt");assertEquals("",Srt.at(c,999));assertEquals("Xin chào",Srt.at(c,1000));assertEquals("Tạm biệt",Srt.at(c,2000));assertEquals("",Srt.at(c,3000));}
 @Test(expected=IllegalArgumentException.class) public void reversed(){Srt.parse("1\n00:00:02,000 --> 00:00:01,000\nInvalid");}
 @Test public void empty(){assertTrue(Srt.parse("").isEmpty());}
 @Test public void whitespaceSeparators(){var c=Srt.parse("1\n00:00:01,000 --> 00:00:02,000\nXin chào\n \n\t\n2\n00:00:02,000 --> 00:00:03,000\nTạm biệt");assertEquals(2,c.size());assertEquals("Xin chào",Srt.at(c,1500));assertEquals("Tạm biệt",Srt.at(c,2500));}
 @org.junit.Test public void webVttWithIdentifierAndSettings(){java.util.List<Srt.Cue> cues=Srt.parse("WEBVTT\n\nNOTE comment\n\nintro\n00:01.250 --> 00:03.500 align:start\nXin chào\nDòng hai\n\n00:04.000 --> 00:05.000\nTạm biệt");org.junit.Assert.assertEquals(2,cues.size());org.junit.Assert.assertEquals(1250L,cues.get(0).start);org.junit.Assert.assertEquals(3500L,cues.get(0).end);org.junit.Assert.assertEquals("Xin chào\nDòng hai",cues.get(0).text);}
 @org.junit.Test(expected=IllegalArgumentException.class) public void rejectsReversedVtt(){Srt.parse("WEBVTT\n\n00:03.000 --> 00:01.000\nInvalid");}
 @Test public void assDialogue(){var cues=Srt.parse("[Script Info]\nTitle: Test\n[Events]\nFormat: Layer, Start, End, Style, Text\nDialogue: 0,0:00:01.25,0:00:03.50,Default,{\\i1}Xin chào, bạn\\NDòng hai");assertEquals(1,cues.size());assertEquals(1250L,cues.get(0).start);assertEquals(3500L,cues.get(0).end);assertEquals("Xin chào, bạn\nDòng hai",cues.get(0).text);}
 @Test(expected=IllegalArgumentException.class) public void rejectsReversedAss(){Srt.parse("[Events]\nFormat: Start, End, Text\nDialogue: 0:00:03.00,0:00:01.00,Invalid");}
}
