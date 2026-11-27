package dev.capcap.mobile;
import android.content.Context;
import android.net.Uri;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;
final class AiVoice {
 static final int RATE=24000;
 static void render(Context context,Uri uri,String key,String voice,List<Srt.Cue> cues)throws Exception{
 long end=0;for(Srt.Cue c:cues)end=Math.max(end,c.end);
 if(end>1800000||cues.size()>1000)throw new IOException("Tối đa 30 phút và 1000 câu");
 File temp=File.createTempFile("dub-",".wav",context.getCacheDir());
 try{
 try(RandomAccessFile out=new RandomAccessFile(temp,"rw")){
 long samples=end*RATE/1000;out.writeBytes("RIFF");le(out,36+samples*2,4);out.writeBytes("WAVEfmt ");le(out,16,4);le(out,1,2);le(out,1,2);le(out,RATE,4);le(out,RATE*2,4);le(out,2,2);le(out,16,2);out.writeBytes("data");le(out,samples*2,4);out.setLength(44+samples*2);
 for(Srt.Cue c:cues){byte[] pcm=speech(key,voice,c.text);long start=c.start*RATE/1000;int count=(int)Math.min(pcm.length/2,(c.end-c.start)*RATE/1000);out.seek(44+start*2);out.write(pcm,0,count*2);}
 }
 try(InputStream in=new FileInputStream(temp);OutputStream out=context.getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IOException("Không mở được tệp xuất");byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}
 }finally{if(!temp.delete())temp.deleteOnExit();}
 }
 static void le(RandomAccessFile out,long value,int bytes)throws IOException{for(int i=0;i<bytes;i++)out.write((int)(value>>(8*i))&255);}
 static byte[] speech(String key,String voice,String text)throws Exception{
 HttpsURLConnection c=(HttpsURLConnection)new URL("https://api.openai.com/v1/audio/speech").openConnection();
 try{c.setConnectTimeout(20000);c.setReadTimeout(120000);c.setInstanceFollowRedirects(false);c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Authorization","Bearer "+key);c.setRequestProperty("Content-Type","application/json");
 JSONObject json=new JSONObject().put("model","gpt-4o-mini-tts").put("voice",voice).put("input",text).put("response_format","pcm");
 try(OutputStream out=c.getOutputStream()){out.write(json.toString().getBytes(StandardCharsets.UTF_8));}
 int status=c.getResponseCode();if(status!=200)throw new IOException("TTS HTTP "+status);
 ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=c.getInputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>10000000)throw new IOException("Câu thoại quá dài");out.write(b,0,n);}}
 return out.toByteArray();
 }finally{c.disconnect();}
 }
}
