package dev.capcap.mobile;
import androidx.media3.common.C;
import androidx.media3.common.audio.AudioProcessor;
import androidx.media3.common.audio.BaseAudioProcessor;
import java.nio.ByteBuffer;
import java.util.List;
@androidx.media3.common.util.UnstableApi
final class DuckProcessor extends BaseAudioProcessor {
 final DuckEnvelope envelope;long frames;
 DuckProcessor(List<Srt.Cue> cues,float low){this.envelope=new DuckEnvelope(cues,low);}
 @Override protected AudioFormat onConfigure(AudioFormat input)throws AudioProcessor.UnhandledAudioFormatException{if(input.encoding!=C.ENCODING_PCM_16BIT)throw new AudioProcessor.UnhandledAudioFormatException(input);return input;}
 @Override protected void onFlush(){frames=0;envelope.reset();}
 @Override public void queueInput(ByteBuffer input){
 ByteBuffer out=replaceOutputBuffer(input.remaining());int channels=inputAudioFormat.channelCount;int rate=inputAudioFormat.sampleRate;
 while(input.remaining()>=channels*2){long ms=frames*1000/rate;float factor=envelope.gain(ms);for(int ch=0;ch<channels;ch++)out.putShort((short)Math.round(input.getShort()*factor));frames++;}
 out.flip();
 }
}
