package dev.capcap.mobile;
import androidx.media3.common.C;
import androidx.media3.common.audio.AudioProcessor;
import androidx.media3.common.audio.BaseAudioProcessor;
import java.nio.ByteBuffer;
import java.util.List;
@androidx.media3.common.util.UnstableApi
final class DuckProcessor extends BaseAudioProcessor {
 final List<Srt.Cue> cues;final float low;long frames;int cursor;
 DuckProcessor(List<Srt.Cue> cues,float low){this.cues=cues;this.low=low;}
 @Override protected AudioFormat onConfigure(AudioFormat input)throws AudioProcessor.UnhandledAudioFormatException{if(input.encoding!=C.ENCODING_PCM_16BIT)throw new AudioProcessor.UnhandledAudioFormatException(input);return input;}
 @Override protected void onFlush(){frames=0;cursor=0;}
 @Override public void queueInput(ByteBuffer input){
 ByteBuffer out=replaceOutputBuffer(input.remaining());int channels=inputAudioFormat.channelCount;int rate=inputAudioFormat.sampleRate;
 while(input.remaining()>=channels*2){long ms=frames*1000/rate;while(cursor<cues.size()&&cues.get(cursor).end+200<ms)cursor++;float factor=1f;for(int i=cursor;i<cues.size();i++){Srt.Cue cue=cues.get(i);if(cue.start-150>ms)break;float weight=ms<cue.start?Math.max(0f,1f-(cue.start-ms)/150f):ms>cue.end?Math.max(0f,1f-(ms-cue.end)/200f):1f;factor=Math.min(factor,1f-(1f-low)*weight);}for(int ch=0;ch<channels;ch++)out.putShort((short)Math.round(input.getShort()*factor));frames++;}
 out.flip();
 }
}
