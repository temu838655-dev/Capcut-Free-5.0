package dev.capcap.mobile;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.AbsoluteSizeSpan;
import androidx.media3.effect.TextOverlay;
import androidx.media3.effect.OverlaySettings;
import java.util.List;
@androidx.media3.common.util.UnstableApi
final class CaptionOverlay extends TextOverlay {
 final List<Srt.Cue> cues;final int size,color,position;final boolean background;
 CaptionOverlay(List<Srt.Cue> cues,int size,int color,int position,boolean background){this.cues=cues;this.size=size;this.color=color;this.position=position;this.background=background;}
 @Override public SpannableString getText(long presentationTimeUs){
 String value=Srt.at(cues,presentationTimeUs/1000);
 SpannableString text=new SpannableString(value.isEmpty()?" ":value);
 if(!value.isEmpty()){text.setSpan(new ForegroundColorSpan(color),0,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);if(background)text.setSpan(new BackgroundColorSpan(0xcc000000),0,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);text.setSpan(new AbsoluteSizeSpan(size),0,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);}
 return text;
 }
 @Override public OverlaySettings getOverlaySettings(long presentationTimeUs){return new OverlaySettings.Builder().setBackgroundFrameAnchor(0f,position==2?0.82f:position==1?0f:-0.82f).setOverlayFrameAnchor(0f,position==2?1f:position==1?0f:-1f).build();}
}
