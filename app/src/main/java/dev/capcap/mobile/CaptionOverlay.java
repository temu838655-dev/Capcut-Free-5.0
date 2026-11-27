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
 final List<Srt.Cue> cues;
 CaptionOverlay(List<Srt.Cue> cues){this.cues=cues;}
 @Override public SpannableString getText(long presentationTimeUs){
 String value=Srt.at(cues,presentationTimeUs/1000);
 SpannableString text=new SpannableString(value.isEmpty()?" ":value);
 if(!value.isEmpty()){text.setSpan(new ForegroundColorSpan(Color.WHITE),0,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);text.setSpan(new BackgroundColorSpan(0xcc000000),0,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);text.setSpan(new AbsoluteSizeSpan(42),0,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);}
 return text;
 }
 @Override public OverlaySettings getOverlaySettings(long presentationTimeUs){return new OverlaySettings.Builder().setBackgroundFrameAnchor(0f,-0.82f).setOverlayFrameAnchor(0f,-1f).build();}
}
