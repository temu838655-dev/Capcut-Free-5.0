package dev.capcap.mobile;
import android.content.Context;
import android.net.Uri;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.*;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.android.gms.tasks.Tasks;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.IOException;
final class VideoOcr {
 interface Progress {void update(int percent);}
 static String scan(Context context,Uri uri,AtomicBoolean cancel,Progress progress)throws Exception{
 MediaMetadataRetriever retriever=new MediaMetadataRetriever();
 TextRecognizer recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
 try{
 retriever.setDataSource(context,uri);
 String value=retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
 if(value==null)throw new IOException("Không xác định được độ dài video");
 long duration=Long.parseLong(value);if(duration<=0||duration>1800000)throw new IOException("OCR hỗ trợ video tối đa 30 phút");
 StringBuilder out=new StringBuilder();String previous="";long start=0;int index=1;
 for(long ms=0;ms<duration;ms+=500){
 if(cancel.get())return "";
 Bitmap frame=null,crop=null,scaled=null;String current;
 try{
 if(android.os.Build.VERSION.SDK_INT>=27)frame=retriever.getScaledFrameAtTime(ms*1000,MediaMetadataRetriever.OPTION_CLOSEST,1280,720);
 else frame=retriever.getFrameAtTime(ms*1000,MediaMetadataRetriever.OPTION_CLOSEST);
 if(frame==null)throw new IOException("Không đọc được khung hình");
 int y=frame.getHeight()*60/100;
 crop=Bitmap.createBitmap(frame,0,y,frame.getWidth(),frame.getHeight()-y);
 scaled=crop.getWidth()>1280?Bitmap.createScaledBitmap(crop,1280,Math.max(1,crop.getHeight()*1280/crop.getWidth()),true):crop;
 current=Tasks.await(recognizer.process(InputImage.fromBitmap(scaled,0)),30,TimeUnit.SECONDS).getText().trim().replaceAll("\\s+"," ");
 }finally{if(scaled!=null&&scaled!=crop&&scaled!=frame)scaled.recycle();if(crop!=null&&crop!=frame)crop.recycle();if(frame!=null)frame.recycle();}
 if(!current.equals(previous)){if(!previous.isEmpty())append(out,index++,start,ms,previous);previous=current;start=ms;}
 progress.update((int)(ms*100/duration));
 }
 if(!previous.isEmpty())append(out,index,start,duration,previous);
 if(out.length()==0)throw new IOException("Không tìm thấy phụ đề");progress.update(100);return out.toString();
 }finally{recognizer.close();retriever.release();}
 }
 static void append(StringBuilder out,int index,long start,long end,String text){if(end>start)out.append(index).append('\n').append(Srt.format(start)).append(" --> ").append(Srt.format(end)).append('\n').append(text).append("\n\n");}
}
