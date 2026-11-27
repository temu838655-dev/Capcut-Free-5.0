package dev.capcap.mobile;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
public class OfflineVoiceTest {
 private File wav(int channels,int rate,int... samples)throws IOException{
 File f=File.createTempFile("voice-test",".wav");
 try(RandomAccessFile out=new RandomAccessFile(f,"rw")){
 out.writeBytes("RIFF");AiVoice.le(out,36+samples.length*2,4);out.writeBytes("WAVEfmt ");AiVoice.le(out,16,4);AiVoice.le(out,1,2);AiVoice.le(out,channels,2);AiVoice.le(out,rate,4);AiVoice.le(out,rate*channels*2,4);AiVoice.le(out,channels*2,2);AiVoice.le(out,16,2);out.writeBytes("data");AiVoice.le(out,samples.length*2,4);for(int s:samples)AiVoice.le(out,s,2);
 }return f;
 }
 @Test public void monoPreservesSamples()throws Exception{File f=wav(1,24000,1000,-1000);try{assertArrayEquals(new byte[]{(byte)232,3,24,(byte)252},OfflineVoice.readPcm(f));}finally{f.delete();}}
 @Test public void stereoDownmix()throws Exception{File f=wav(2,24000,1000,-1000,2000,0);try{assertArrayEquals(new byte[]{0,0,(byte)232,3},OfflineVoice.readPcm(f));}finally{f.delete();}}
 @Test public void resamplesTo24k()throws Exception{File f=wav(1,12000,0,1000);try{assertEquals(8,OfflineVoice.readPcm(f).length);}finally{f.delete();}}
 @Test(expected=IOException.class) public void rejectsIncompleteStereo()throws Exception{File f=wav(2,24000,1000,2000,3000);try{OfflineVoice.readPcm(f);}finally{f.delete();}}
}
