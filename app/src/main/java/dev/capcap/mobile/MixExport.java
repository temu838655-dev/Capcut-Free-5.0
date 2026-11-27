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
 final Activity activity;final Uri video,audio,target;final Runnable done;final float originalGain,voiceGain;final int height;final java.util.List<Srt.Cue> captions,duckCues;final int captionSize,captionColor,captionPosition;final boolean captionBackground;File temp;Transformer transformer;ProgressDialog dialog;volatile boolean canceled;boolean copying,finished;final android.os.Handler handler=new android.os.Handler(android.os.Looper.getMainLooper());final Runnable poll=new Runnable(){public void run(){if(finished||copying||canceled)return;ProgressHolder holder=new ProgressHolder();if(transformer.getProgress(holder)==Transformer.PROGRESS_STATE_AVAILABLE)dialog.setProgress(holder.progress);handler.postDelayed(this,500);}};
 MixExport(Activity a,Uri v,Uri s,Uri t,float g,float h,int height,java.util.List<Srt.Cue> captions,int captionSize,int captionColor,int captionPosition,boolean captionBackground,java.util.List<Srt.Cue> duckCues,Runnable d){activity=a;video=v;audio=s;target=t;originalGain=g;voiceGain=h;this.height=height;this.captions=new java.util.ArrayList<>(captions);this.captionSize=captionSize;this.captionColor=captionColor;this.captionPosition=captionPosition;this.captionBackground=captionBackground;this.duckCues=new java.util.ArrayList<>(duckCues);done=d;}
 void start()throws IOException{
 try{startInternal();}catch(IOException|RuntimeException e){finish(e.getMessage()==null?e.getClass().getSimpleName():e.getMessage());}
 }
 private void startInternal()throws IOException{
 temp=File.createTempFile("mix-",".mp4",activity.getCacheDir());
 ChannelMixingAudioProcessor gain=new ChannelMixingAudioProcessor();
 gain.putChannelMixingMatrix(ChannelMixingMatrix.create(1,1).scaleBy(originalGain));
 gain.putChannelMixingMatrix(ChannelMixingMatrix.create(2,2).scaleBy(originalGain));
 ImmutableList.Builder<androidx.media3.common.Effect> effects=ImmutableList.builder();if(height!=0)effects.add(androidx.media3.effect.Presentation.createForHeight(height));if(!captions.isEmpty())effects.add(new androidx.media3.effect.OverlayEffect(ImmutableList.of(new CaptionOverlay(captions,captionSize,captionColor,captionPosition,captionBackground))));
 EditedMediaItem original=new EditedMediaItem.Builder(MediaItem.fromUri(video)).setEffects(new Effects(duckCues.isEmpty()?ImmutableList.of(gain):ImmutableList.of(new DuckProcessor(duckCues,originalGain)),effects.build())).build();
 ChannelMixingAudioProcessor voiceProcessor=new ChannelMixingAudioProcessor();voiceProcessor.putChannelMixingMatrix(ChannelMixingMatrix.create(1,1).scaleBy(voiceGain));voiceProcessor.putChannelMixingMatrix(ChannelMixingMatrix.create(2,2).scaleBy(voiceGain));
 EditedMediaItem voice=new EditedMediaItem.Builder(MediaItem.fromUri(audio)).setRemoveVideo(true).setEffects(new Effects(ImmutableList.of(voiceProcessor),ImmutableList.of())).build();
 Composition composition=new Composition.Builder(ImmutableList.of(new EditedMediaItemSequence(ImmutableList.of(original)),new EditedMediaItemSequence(ImmutableList.of(voice)))).build();
 transformer=new Transformer.Builder(activity).addListener(new Transformer.Listener(){
 @Override public void onCompleted(Composition c,ExportResult r){if(canceled||finished)return;copying=true;handler.removeCallbacks(poll);dialog.setMessage("Đang lưu video...");dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE).setEnabled(false);new Thread(()->{String error=null;try(InputStream in=new FileInputStream(temp);OutputStream out=activity.getContentResolver().openOutputStream(target,"wt")){if(out==null)throw new IOException("Không mở được tệp xuất");byte[] b=new byte[65536];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}catch(Exception e){error=e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();}final String message=error;activity.runOnUiThread(()->finish(message));}).start();}
 @Override public void onError(Composition c,ExportResult r,ExportException e){finish(e.getMessage()==null?e.getClass().getSimpleName():e.getMessage());}
 }).build();
 dialog=new ProgressDialog(activity);dialog.setMessage("Đang trộn âm thanh và xuất MP4");dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);dialog.setMax(100);dialog.setCancelable(false);dialog.setButton(android.content.DialogInterface.BUTTON_NEGATIVE,"Hủy",(d,w)->cancel());dialog.show();
 try{transformer.start(composition,temp.getAbsolutePath());handler.post(poll);}catch(RuntimeException e){finish(e.getMessage());}
 }
 void cancel(){if(finished||copying)return;canceled=true;try{if(transformer!=null)transformer.cancel();}finally{finish(null);}}
 void finish(String error){if(finished)return;finished=true;handler.removeCallbacks(poll);if(temp!=null&&!temp.delete())temp.deleteOnExit();if(!activity.isDestroyed()){if(dialog!=null)dialog.dismiss();if(!canceled)Toast.makeText(activity,error==null?"Đã xuất video trộn âm":"Xuất thất bại: "+error,Toast.LENGTH_LONG).show();}done.run();}
}
