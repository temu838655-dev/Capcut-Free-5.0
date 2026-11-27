package dev.capcap.mobile;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class DuckEnvelopeTest {
 @Test public void transitions(){DuckEnvelope e=new DuckEnvelope(Collections.singletonList(new Srt.Cue(1000,2000,"a")),0.25f);assertEquals(1f,e.gain(800),.001f);assertEquals(.625f,e.gain(925),.001f);assertEquals(.25f,e.gain(1000),.001f);assertEquals(.625f,e.gain(2100),.001f);assertEquals(1f,e.gain(2200),.001f);}
 @Test public void overlap(){DuckEnvelope e=new DuckEnvelope(Arrays.asList(new Srt.Cue(1000,2000,"a"),new Srt.Cue(1500,3000,"b")),0f);assertEquals(0f,e.gain(2100),.001f);assertEquals(1f,e.gain(3300),.001f);e.reset();assertEquals(0f,e.gain(1200),.001f);}
 @Test public void noCues(){assertEquals(1f,new DuckEnvelope(Collections.emptyList(),0f).gain(100),.001f);}
}
