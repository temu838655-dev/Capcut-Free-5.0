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
 for(Srt.Cue c:cues){byte[] pcm=speech(key,voice,c.text);long start=c.start*RATE/1000;int capacity=(int)((c.end-c.start)*RATE/1000);byte[] fitted=fit(pcm,capacity);out.seek(44+start*2);byte[] existing=new byte[fitted.length];out.readFully(existing);for(int i=0;i<fitted.length;i+=2){int x=(short)((existing[i]&255)|(existing[i+1]<<8));int y=(short)((fitted[i]&255)|(fitted[i+1]<<8));int mixed=Math.max(-32768,Math.min(32767,x+y));fitted[i]=(byte)mixed;fitted[i+1]=(byte)(mixed>>8);}out.seek(44+start*2);out.write(fitted);}
 }
 try(InputStream in=new FileInputStream(temp);OutputStream out=context.getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IOException("Không mở được tệp xuất");byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}
 }finally{if(!temp.delete())temp.deleteOnExit();}
 }
 static byte[] fit(byte[] pcm,int capacity)throws IOException{
 if((pcm.length&1)!=0||pcm.length==0||capacity<=0)throw new IOException("PCM hoặc thời lượng không hợp lệ");
 int n=pcm.length/2;if(n<=capacity)return pcm;
 if(n>capacity*2L)throw new IOException("Câu thoại dài hơn gấp đôi thời lượng; hãy chỉnh phụ đề");
 byte[] result=new byte[capacity*2];for(int i=0;i<capacity;i++){double pos=capacity==1?0:(double)i*(n-1)/(capacity-1);int j=(int)pos,k=Math.min(n-1,j+1);int x=(short)((pcm[j*2]&255)|(pcm[j*2+1]<<8));int y=(short)((pcm[k*2]&255)|(pcm[k*2+1]<<8));int sample=(int)Math.round(x+(y-x)*(pos-j));result[i*2]=(byte)sample;result[i*2+1]=(byte)(sample>>8);}return result;
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
