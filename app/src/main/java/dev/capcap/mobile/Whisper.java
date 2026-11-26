package dev.capcap.mobile;
import android.content.Context;
import android.net.Uri;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;
final class Whisper {
 static String transcribe(Context context,Uri uri,String key)throws Exception{
 File temp=File.createTempFile("speech-",".audio",context.getCacheDir());
 try{
 String mime=context.getContentResolver().getType(uri);
 String ext="audio/mpeg".equals(mime)?"mp3":"audio/wav".equals(mime)||"audio/x-wav".equals(mime)?"wav":"audio/mp4".equals(mime)||"audio/x-m4a".equals(mime)?"m4a":"audio/webm".equals(mime)?"webm":"audio/flac".equals(mime)?"flac":null;
 if(ext==null)throw new IOException("Hỗ trợ MP3, WAV, M4A, WebM, FLAC");
 try(InputStream in=context.getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(temp)){if(in==null)throw new IOException("Không đọc được âm thanh");byte[] b=new byte[8192];int n;long size=0;while((n=in.read(b))!=-1){size+=n;if(size>24000000)throw new IOException("Âm thanh vượt 24 MB");out.write(b,0,n);}}
 if(temp.length()==0)throw new IOException("Tệp rỗng");
 String boundary="Capcap"+java.util.UUID.randomUUID().toString();
 HttpsURLConnection c=(HttpsURLConnection)new URL("https://api.openai.com/v1/audio/transcriptions").openConnection();
 try{c.setConnectTimeout(20000);c.setReadTimeout(180000);c.setInstanceFollowRedirects(false);c.setRequestMethod("POST");c.setRequestProperty("Authorization","Bearer "+key);c.setRequestProperty("Content-Type","multipart/form-data; boundary="+boundary);c.setDoOutput(true);c.setChunkedStreamingMode(8192);
 try(OutputStream out=c.getOutputStream();InputStream in=new FileInputStream(temp)){
 write(out,"--"+boundary+"\r\nContent-Disposition: form-data; name=\"model\"\r\n\r\nwhisper-1\r\n--"+boundary+"\r\nContent-Disposition: form-data; name=\"response_format\"\r\n\r\nsrt\r\n--"+boundary+"\r\nContent-Disposition: form-data; name=\"file\"; filename=\"speech."+ext+"\"\r\nContent-Type: "+mime+"\r\n\r\n");
 byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);write(out,"\r\n--"+boundary+"--\r\n");}
 int status=c.getResponseCode();if(status!=200)throw new IOException("HTTP "+status);
 ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=c.getInputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>2000000)throw new IOException("Phụ đề quá lớn");out.write(b,0,n);}}
 String result=new String(out.toByteArray(),StandardCharsets.UTF_8);if(Srt.parse(result).isEmpty())throw new IOException("Không tìm thấy lời thoại");return result;
 }finally{c.disconnect();}
 }finally{if(!temp.delete())temp.deleteOnExit();}
 }
 static void write(OutputStream out,String text)throws IOException{out.write(text.getBytes(StandardCharsets.UTF_8));}
}
