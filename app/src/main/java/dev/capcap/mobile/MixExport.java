package dev.capcap.mobile;
import android.app.*;
import android.net.Uri;
import android.widget.Toast;
import androidx.media3.common.MediaItem;
import androidx.media3.common.audio.ChannelMixingAudioProcessor;
import androidx.media3.common.audio.ChannelMixingMatrix;
import androidx.media3.transformer.*;
import com.google.common.collect.ImmutableList;
import java.io.*;
@androidx.media3.common.util.UnstableApi
final class MixExport {
 final Activity activity;final Uri video,audio,target;final Runnable done;final float originalGain,voiceGain;File temp;Transformer transformer;ProgressDialog dialog;boolean canceled;
 MixExport(Activity a,Uri v,Uri s,Uri t,float g,float h,Runnable d){activity=a;video=v;audio=s;target=t;originalGain=g;voiceGain=h;done=d;}
 void start()throws IOException{
 temp=File.createTempFile("mix-",".mp4",activity.getCacheDir());
 ChannelMixingAudioProcessor gain=new ChannelMixingAudioProcessor();
 gain.putChannelMixingMatrix(ChannelMixingMatrix.create(1,1).scaleBy(originalGain));
 gain.putChannelMixingMatrix(ChannelMixingMatrix.create(2,2).scaleBy(originalGain));
 EditedMediaItem original=new EditedMediaItem.Builder(MediaItem.fromUri(video)).setEffects(new Effects(ImmutableList.of(gain),ImmutableList.of())).build();
 ChannelMixingAudioProcessor voiceProcessor=new ChannelMixingAudioProcessor();voiceProcessor.putChannelMixingMatrix(ChannelMixingMatrix.create(1,1).scaleBy(voiceGain));voiceProcessor.putChannelMixingMatrix(ChannelMixingMatrix.create(2,2).scaleBy(voiceGain));
 EditedMediaItem voice=new EditedMediaItem.Builder(MediaItem.fromUri(audio)).setRemoveVideo(true).setEffects(new Effects(ImmutableList.of(voiceProcessor),ImmutableList.of())).build();
 Composition composition=new Composition.Builder(ImmutableList.of(new EditedMediaItemSequence(ImmutableList.of(original)),new EditedMediaItemSequence(ImmutableList.of(voice)))).build();
 transformer=new Transformer.Builder(activity).addListener(new Transformer.Listener(){
 @Override public void onCompleted(Composition c,ExportResult r){new Thread(()->{String error=null;try(InputStream in=new FileInputStream(temp);OutputStream out=activity.getContentResolver().openOutputStream(target,"wt")){if(out==null)throw new IOException("Không mở được tệp xuất");byte[] b=new byte[65536];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}catch(Exception e){error=e.getMessage();}final String message=error;activity.runOnUiThread(()->finish(message));}).start();}
 @Override public void onError(Composition c,ExportResult r,ExportException e){finish(e.getMessage());}
 }).build();
 dialog=new ProgressDialog(activity);dialog.setMessage("Đang trộn âm thanh và xuất MP4");dialog.setCancelable(false);dialog.setButton(android.content.DialogInterface.BUTTON_NEGATIVE,"Hủy",(d,w)->cancel());dialog.show();
 try{transformer.start(composition,temp.getAbsolutePath());}catch(RuntimeException e){finish(e.getMessage());}
 }
 void cancel(){canceled=true;if(transformer!=null)transformer.cancel();finish(null);}
 void finish(String error){if(temp!=null&&!temp.delete())temp.deleteOnExit();if(!activity.isDestroyed()){if(dialog!=null)dialog.dismiss();if(!canceled)Toast.makeText(activity,error==null?"Đã xuất video trộn âm":"Xuất thất bại: "+error,Toast.LENGTH_LONG).show();}done.run();}
}
