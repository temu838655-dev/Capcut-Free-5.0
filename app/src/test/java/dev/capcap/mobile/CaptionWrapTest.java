package dev.capcap.mobile;
import org.junit.Test;
import static org.junit.Assert.*;
public class CaptionWrapTest {
 @Test public void wrapsWords(){assertEquals("xin chào\nbạn",CaptionWrap.wrap("xin chào bạn",8,String::length));}
 @Test public void preservesLines(){assertEquals("a\nb",CaptionWrap.wrap("a\nb",10,String::length));}
 @Test public void splitsLongWords(){assertEquals("abc\ndef",CaptionWrap.wrap("abcdef",3,String::length));}
 @Test public void empty(){assertEquals("",CaptionWrap.wrap("",3,String::length));}
 @Test public void keepsCombiningAccents(){assertEquals("a\u0301\nb",CaptionWrap.wrap("a\u0301b",2,String::length));assertEquals("a\u0301\nb",CaptionWrap.wrap("a\u0301b",1,String::length));}
}
