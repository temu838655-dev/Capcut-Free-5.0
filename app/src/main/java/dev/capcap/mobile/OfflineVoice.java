package dev.capcap.mobile;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import java.io.*;
import java.util.concurrent.*;
final class OfflineVoice implements AutoCloseable,AiVoice.SpeechSource {
 private final Context context;
 private final TextToSpeech tts;
 OfflineVoice(Context context,String name)throws Exception{
 this.context=context.getApplicationContext();
 CountDownLatch ready=new CountDownLatch(1);int[] status={TextToSpeech.ERROR};
 tts=new TextToSpeech(this.context,s->{status[0]=s;ready.countDown();});
 try{if(!ready.await(30,TimeUnit.SECONDS)||status[0]!=TextToSpeech.SUCCESS)throw new IOException("TTS chưa sẵn sàng");
 Voice selected=null;java.util.Set<Voice> voices=tts.getVoices();if(voices!=null)for(Voice v:voices)if(v.getName().equals(name)&&"vi".equals(v.getLocale().getLanguage())&&!v.isNetworkConnectionRequired()&&(v.getFeatures()==null||!v.getFeatures().contains("notInstalled")))selected=v;
 if(selected==null||tts.setVoice(selected)!=TextToSpeech.SUCCESS)throw new IOException("Giọng Việt offline không khả dụng");
 }catch(Exception e){tts.shutdown();throw e;}
 }
 public byte[] synthesize(String text,double speed)throws Exception{
 File file=File.createTempFile("offline-",".wav",context.getCacheDir());
 try{CountDownLatch done=new CountDownLatch(1);boolean[] ok={false};String id=java.util.UUID.randomUUID().toString();
 tts.setOnUtteranceProgressListener(new UtteranceProgressListener(){public void onStart(String s){}public void onDone(String s){if(id.equals(s)){ok[0]=true;done.countDown();}}public void onError(String s){if(id.equals(s))done.countDown();}});
 if(tts.setSpeechRate((float)speed)!=TextToSpeech.SUCCESS||tts.synthesizeToFile(text,new android.os.Bundle(),file,id)!=TextToSpeech.SUCCESS||!done.await(90,TimeUnit.SECONDS)||!ok[0]){tts.stop();throw new IOException("Không tạo được giọng offline");}
 return readPcm(file);
 }finally{file.delete();}
 }
 static byte[] readPcm(File file)throws IOException{
 try(RandomAccessFile in=new RandomAccessFile(file,"r")){
 if(in.readInt()!=0x52494646)throw new IOException("WAV không hợp lệ");in.skipBytes(4);if(in.readInt()!=0x57415645)throw new IOException("WAV không hợp lệ");
 int rate=0,channels=0,bits=0,format=0;byte[] data=null;
 while(in.getFilePointer()+8<=in.length()){int tag=in.readInt();long size=Integer.toUnsignedLong(Integer.reverseBytes(in.readInt()));long next=in.getFilePointer()+size;if(next>in.length())throw new IOException("WAV thiếu dữ liệu");
 if(tag==0x666d7420&&size>=16){format=Short.toUnsignedInt(Short.reverseBytes(in.readShort()));channels=Short.toUnsignedInt(Short.reverseBytes(in.readShort()));rate=Integer.reverseBytes(in.readInt());in.skipBytes(6);bits=Short.toUnsignedInt(Short.reverseBytes(in.readShort()));}
 else if(tag==0x64617461){if(size>10000000)throw new IOException("Câu thoại quá dài");data=new byte[(int)size];in.readFully(data);}
 in.seek(Math.min(in.length(),next+(size&1)));}
 if(format!=1||bits!=16||channels<1||channels>2||rate<8000||rate>96000||data==null||data.length<channels*2)throw new IOException("Định dạng PCM không hỗ trợ");
 int frames=data.length/(channels*2),count=(int)Math.max(1,(long)frames*AiVoice.RATE/rate);byte[] out=new byte[count*2];
 for(int i=0;i<count;i++){double pos=(double)i*rate/AiVoice.RATE;int a=Math.min(frames-1,(int)pos),b=Math.min(frames-1,a+1);int sample=(int)Math.round(sample(data,a,channels)+(sample(data,b,channels)-sample(data,a,channels))*(pos-a));out[2*i]=(byte)sample;out[2*i+1]=(byte)(sample>>8);}return out;
 }}
 private static int sample(byte[] data,int frame,int channels){int sum=0;for(int c=0;c<channels;c++){int i=(frame*channels+c)*2;sum+=(short)((data[i]&255)|(data[i+1]<<8));}return sum/channels;}
 public void close(){tts.stop();tts.shutdown();}
}
