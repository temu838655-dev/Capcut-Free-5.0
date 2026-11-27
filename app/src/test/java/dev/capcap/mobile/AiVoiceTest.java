package dev.capcap.mobile;
import org.junit.Test;
import static org.junit.Assert.*;
public class AiVoiceTest {
 @Test public void preservesShortAudio()throws Exception{byte[] pcm={1,0,2,0};assertArrayEquals(pcm,AiVoice.fit(pcm,4));}
 @Test public void fitsLongAudio()throws Exception{assertArrayEquals(new byte[]{1,0,4,0},AiVoice.fit(new byte[]{1,0,2,0,3,0,4,0},2));}
 @Test(expected=java.io.IOException.class) public void rejectsExcessiveCompression()throws Exception{AiVoice.fit(new byte[10],2);}
}
