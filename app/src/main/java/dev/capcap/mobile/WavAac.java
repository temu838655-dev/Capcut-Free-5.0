package dev.capcap.mobile;
import android.content.Context;
import android.net.Uri;
import android.media.*;
import java.io.*;
import java.nio.ByteBuffer;
final class WavAac {
 static boolean isWav(Context c,Uri uri)throws IOException{
 try(InputStream in=c.getContentResolver().openInputStream(uri)){if(in==null)throw new IOException("Không đọc được âm thanh");byte[] h=new byte[12];new DataInputStream(in).readFully(h);return h[0]=='R'&&h[1]=='I'&&h[2]=='F'&&h[3]=='F'&&h[8]=='W'&&h[9]=='A'&&h[10]=='V'&&h[11]=='E';}
 }
 static int le(DataInputStream in)throws IOException{return Integer.reverseBytes(in.readInt());}
 static File encode(Context c,Uri uri)throws Exception{
 File temp=File.createTempFile("voice-",".m4a",c.getCacheDir());boolean ok=false;MediaCodec codec=null;MediaMuxer mux=null;
 try(DataInputStream in=new DataInputStream(new BufferedInputStream(c.getContentResolver().openInputStream(uri)))){
 byte[] h=new byte[44];in.readFully(h);ByteBuffer b=ByteBuffer.wrap(h).order(java.nio.ByteOrder.LITTLE_ENDIAN);
 if(b.getInt(0)!=0x46464952||b.getInt(8)!=0x45564157||b.getInt(12)!=0x20746d66||b.getInt(16)!=16||b.getShort(20)!=1||b.getShort(22)!=1||b.getInt(24)!=24000||b.getShort(34)!=16||b.getInt(36)!=0x61746164)throw new IOException("Chỉ hỗ trợ WAV PCM mono 24 kHz 16-bit do ứng dụng tạo");
 int remaining=b.getInt(40);if(remaining<=0||remaining>86400000||(remaining&1)!=0)throw new IOException("Kích thước WAV không hợp lệ");
 MediaFormat format=MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,24000,1);format.setInteger(MediaFormat.KEY_AAC_PROFILE,MediaCodecInfo.CodecProfileLevel.AACObjectLC);format.setInteger(MediaFormat.KEY_BIT_RATE,64000);
 codec=MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);codec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);codec.start();mux=new MediaMuxer(temp.getAbsolutePath(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
 boolean inputDone=false,outputDone=false;int track=-1;long samples=0;MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();byte[] chunk=new byte[8192];long lastProgress=android.os.SystemClock.elapsedRealtime();
 while(!outputDone){
 if(android.os.SystemClock.elapsedRealtime()-lastProgress>30000)throw new IOException("Bộ mã hóa AAC không phản hồi");
 if(!inputDone){int index=codec.dequeueInputBuffer(10000);if(index>=0){ByteBuffer buf=codec.getInputBuffer(index);if(buf==null)throw new IOException("Không có bộ đệm AAC");buf.clear();int n=Math.min(remaining,Math.min(chunk.length,buf.remaining()))&~1;if(remaining>0&&n==0)throw new IOException("Bộ đệm quá nhỏ");if(n>0){in.readFully(chunk,0,n);buf.put(chunk,0,n);}codec.queueInputBuffer(index,0,n,samples*1000000L/24000,remaining==0?MediaCodec.BUFFER_FLAG_END_OF_STREAM:0);samples+=n/2;remaining-=n;inputDone=n==0;lastProgress=android.os.SystemClock.elapsedRealtime();}}
 int index=codec.dequeueOutputBuffer(info,10000);if(index==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){if(track>=0)throw new IOException("Định dạng AAC thay đổi");track=mux.addTrack(codec.getOutputFormat());mux.start();lastProgress=android.os.SystemClock.elapsedRealtime();}
 else if(index>=0){ByteBuffer buf=codec.getOutputBuffer(index);if(info.size>0&&(info.flags&MediaCodec.BUFFER_FLAG_CODEC_CONFIG)==0){if(track<0||buf==null)throw new IOException("AAC chưa sẵn sàng");buf.position(info.offset);buf.limit(info.offset+info.size);mux.writeSampleData(track,buf,info);}outputDone=(info.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0;codec.releaseOutputBuffer(index,false);lastProgress=android.os.SystemClock.elapsedRealtime();}
 }
 mux.stop();ok=true;return temp;
 }finally{if(codec!=null)codec.release();if(mux!=null)mux.release();if(!ok&&!temp.delete())temp.deleteOnExit();}
 }
}
