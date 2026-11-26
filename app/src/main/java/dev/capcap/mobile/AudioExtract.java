package dev.capcap.mobile;
import android.content.Context;
import android.net.Uri;
import android.media.*;
import java.io.*;
import java.nio.ByteBuffer;
final class AudioExtract {
 static File extract(Context context,Uri uri)throws IOException{
 File out=File.createTempFile("extracted-",".m4a",context.getCacheDir());
 MediaExtractor extractor=new MediaExtractor();MediaMuxer muxer=null;boolean success=false,started=false;
 try{
 extractor.setDataSource(context,uri,null);int track=-1;MediaFormat format=null;
 for(int i=0;i<extractor.getTrackCount();i++){MediaFormat candidate=extractor.getTrackFormat(i);if("audio/mp4a-latm".equals(candidate.getString(MediaFormat.KEY_MIME))){track=i;format=candidate;break;}}
 if(track<0||format==null)throw new IOException("Video chưa có track AAC hỗ trợ; hãy nhập tệp âm thanh riêng");
 extractor.selectTrack(track);muxer=new MediaMuxer(out.getAbsolutePath(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);int target=muxer.addTrack(format);muxer.start();started=true;
 ByteBuffer buffer=ByteBuffer.allocateDirect(1024*1024);MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();long total=0;long first=-1;
 while(true){buffer.clear();int n=extractor.readSampleData(buffer,0);if(n<0)break;if(n>buffer.capacity())throw new IOException("Gói âm thanh quá lớn");total+=n;if(total>23500000)throw new IOException("Âm thanh vượt giới hạn Whisper 24 MB");long time=extractor.getSampleTime();if(first<0)first=time;info.set(0,n,time-first,(extractor.getSampleFlags()&MediaExtractor.SAMPLE_FLAG_SYNC)!=0?MediaCodec.BUFFER_FLAG_KEY_FRAME:0);buffer.position(0);buffer.limit(n);muxer.writeSampleData(target,buffer,info);extractor.advance();}
 if(total==0)throw new IOException("Track âm thanh rỗng");muxer.stop();started=false;success=true;return out;
 }finally{extractor.release();if(muxer!=null){if(started){try{muxer.stop();}catch(RuntimeException ignored){}}muxer.release();}if(!success&&!out.delete())out.deleteOnExit();}
 }
}
