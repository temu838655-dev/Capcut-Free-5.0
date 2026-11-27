package dev.capcap.mobile;
import android.content.Context;
import android.net.Uri;
import android.media.*;
import java.io.*;
import java.nio.ByteBuffer;
final class VideoMux {
 static void replaceAudio(Context context,Uri video,Uri audio,Uri target)throws Exception{
 if(target.equals(video)||target.equals(audio))throw new IOException("Tệp xuất không được trùng tệp nguồn");
 File temp=File.createTempFile("video-",".mp4",context.getCacheDir());MediaExtractor ve=new MediaExtractor(),ae=new MediaExtractor();MediaMuxer mux=null;
 try{
 ve.setDataSource(context,video,null);ae.setDataSource(context,audio,null);
 MediaFormat vf=select(ve,"video/"),af=select(ae,"audio/mp4a-latm");
 if(!vf.containsKey(MediaFormat.KEY_DURATION))throw new IOException("Không xác định được thời lượng video");
 long duration=vf.getLong(MediaFormat.KEY_DURATION);
 if(duration<=0)throw new IOException("Thời lượng video không hợp lệ");
 mux=new MediaMuxer(temp.getAbsolutePath(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
 if(vf.containsKey(MediaFormat.KEY_ROTATION))mux.setOrientationHint(vf.getInteger(MediaFormat.KEY_ROTATION));
 int vt=mux.addTrack(vf),at=mux.addTrack(af);mux.start();
 copy(ve,mux,vt,Long.MAX_VALUE);copy(ae,mux,at,duration);mux.stop();mux.release();mux=null;
 try(InputStream in=new FileInputStream(temp);OutputStream out=context.getContentResolver().openOutputStream(target,"wt")){if(out==null)throw new IOException("Không mở được tệp xuất");byte[] b=new byte[65536];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}
 }finally{ve.release();ae.release();if(mux!=null)mux.release();if(!temp.delete())android.util.Log.w("VideoMux","Không xóa được tệp tạm: "+temp.getName());}
 }
 static MediaFormat select(MediaExtractor e,String prefix)throws IOException{
 for(int i=0;i<e.getTrackCount();i++){MediaFormat f=e.getTrackFormat(i);String mime=f.getString(MediaFormat.KEY_MIME);if(mime!=null&&mime.startsWith(prefix)){e.selectTrack(i);return f;}}
 throw new IOException("Không tìm thấy track "+prefix+" (âm thanh cần AAC)");
 }
 static void copy(MediaExtractor e,MediaMuxer m,int track,long limit)throws IOException{
 ByteBuffer buffer=ByteBuffer.allocateDirect(16*1024*1024);MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();long first=e.getSampleTime();int written=0;if(first<0)throw new IOException("Track rỗng");
 while(e.getSampleTime()>=0){long time=e.getSampleTime()-first;if(time>=limit)break;if(android.os.Build.VERSION.SDK_INT>=28){long sampleSize=e.getSampleSize();if(sampleSize>64L*1024*1024)throw new IOException("Khung dữ liệu vượt 64 MB");if(sampleSize>buffer.capacity())buffer=ByteBuffer.allocateDirect((int)sampleSize);}buffer.clear();int size=e.readSampleData(buffer,0);if(size<0)throw new IOException("Không đọc được khung dữ liệu");if(size==0)throw new IOException("Khung dữ liệu rỗng");if(size>buffer.capacity())throw new IOException("Khung dữ liệu quá lớn");int flags=e.getSampleFlags();if(android.os.Build.VERSION.SDK_INT>=26&&(flags&MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME)!=0)throw new IOException("Không hỗ trợ khung dữ liệu phân mảnh");if((flags&MediaExtractor.SAMPLE_FLAG_ENCRYPTED)!=0)throw new IOException("Không hỗ trợ nội dung mã hóa");info.set(0,size,time,(flags&MediaExtractor.SAMPLE_FLAG_SYNC)!=0?MediaCodec.BUFFER_FLAG_KEY_FRAME:0);buffer.position(0);buffer.limit(size);m.writeSampleData(track,buffer,info);written++;e.advance();}
 if(written==0)throw new IOException("Track không có dữ liệu trong khoảng xuất");
 }
}
