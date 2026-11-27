package dev.capcap.mobile;
import java.util.*;
final class DuckEnvelope {
 final List<Srt.Cue> cues;final float low;int cursor;long lastMs=Long.MIN_VALUE;
 DuckEnvelope(List<Srt.Cue> input,float low){this.low=Math.max(0,Math.min(1,low));cues=new ArrayList<>(input);Collections.sort(cues,(a,b)->Long.compare(a.start,b.start));}
 void reset(){cursor=0;lastMs=Long.MIN_VALUE;}
 float gain(long ms){if(ms<lastMs)reset();lastMs=ms;while(cursor<cues.size()&&cues.get(cursor).end+200<ms)cursor++;float factor=1f;for(int i=cursor;i<cues.size();i++){Srt.Cue cue=cues.get(i);if(cue.start-150>ms)break;float weight=ms<cue.start?Math.max(0f,1f-(cue.start-ms)/150f):ms>cue.end?Math.max(0f,1f-(ms-cue.end)/200f):1f;factor=Math.min(factor,1f-(1f-low)*weight);}return factor;}
}
