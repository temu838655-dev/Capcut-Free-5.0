package dev.capcap.mobile;
import ai.onnxruntime.*;
import java.io.*;
import java.util.*;
final class LocalOnnx implements AutoCloseable {
 private final OrtSession.SessionOptions options;
 private final OrtSession session;
 private boolean closed;
 LocalOnnx(File model)throws Exception{
 if(!model.isFile()||model.length()==0)throw new IOException("Model chưa được cài đặt");
 options=new OrtSession.SessionOptions();
 try{options.setIntraOpNumThreads(Math.max(1,Math.min(4,Runtime.getRuntime().availableProcessors())));options.setInterOpNumThreads(1);
 session=OrtEnvironment.getEnvironment().createSession(model.getAbsolutePath(),options);
 }catch(Exception|LinkageError e){options.close();throw e;}
 }
 synchronized void validateSignature(Set<String> inputs,Set<String> outputs)throws Exception{
 check();if(!session.getInputNames().equals(inputs)||!session.getOutputNames().containsAll(outputs))throw new IOException("Model không tương thích");
 }
 synchronized OrtSession.Result run(Map<String,? extends OnnxTensorLike> inputs)throws Exception{
 check();if(Thread.currentThread().isInterrupted())throw new InterruptedIOException();if(!session.getInputNames().equals(inputs.keySet()))throw new IOException("Thiếu hoặc sai đầu vào model");return session.run(inputs);
 }
 private void check(){if(closed)throw new IllegalStateException("Session closed");}
 public synchronized void close()throws OrtException{if(closed)return;closed=true;try{session.close();}finally{options.close();}}
}
