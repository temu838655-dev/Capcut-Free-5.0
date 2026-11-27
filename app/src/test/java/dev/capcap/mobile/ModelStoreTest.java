package dev.capcap.mobile;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import java.io.*;
public class ModelStoreTest {
 @Rule public TemporaryFolder folder=new TemporaryFolder();
 private static final String SHA="ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
 private InputStream abc(){return new ByteArrayInputStream(new byte[]{97,98,99});}
 @Test public void installsVerifiedModel()throws Exception{File d=folder.newFolder();File f=ModelStore.install(d,"voice.onnx",abc(),3,SHA);assertEquals(3,f.length());assertEquals(1,d.list().length);}
 @Test public void rejectsCorruptionWithoutReplacing()throws Exception{File d=folder.newFolder();File f=ModelStore.install(d,"voice.onnx",abc(),3,SHA);try{ModelStore.install(d,"voice.onnx",new ByteArrayInputStream(new byte[]{1,2,3}),3,SHA);fail();}catch(IOException expected){}try(FileInputStream in=new FileInputStream(f)){assertEquals(97,in.read());}assertEquals(1,d.list().length);}
 @Test(expected=IOException.class) public void rejectsTraversal()throws Exception{ModelStore.install(folder.newFolder(),"../voice",abc(),3,SHA);}
 @Test(expected=IOException.class) public void rejectsOversize()throws Exception{ModelStore.install(folder.newFolder(),"voice",abc(),2,SHA);}
}
