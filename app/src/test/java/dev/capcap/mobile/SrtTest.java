package dev.capcap.mobile;
import org.junit.Test;
import static org.junit.Assert.*;
public class SrtTest {
 @Test public void timing(){var c=Srt.parse("1\r\n00:00:01,000 --> 00:00:02,000\r\nXin chào\r\n\r\n2\r\n00:00:02,000 --> 00:00:03,000\r\nTạm biệt");assertEquals("",Srt.at(c,999));assertEquals("Xin chào",Srt.at(c,1000));assertEquals("Tạm biệt",Srt.at(c,2000));assertEquals("",Srt.at(c,3000));}
 @Test(expected=IllegalArgumentException.class) public void reversed(){Srt.parse("1\n00:00:02,000 --> 00:00:01,000\nInvalid");}
 @Test public void empty(){assertTrue(Srt.parse("").isEmpty());}
}
