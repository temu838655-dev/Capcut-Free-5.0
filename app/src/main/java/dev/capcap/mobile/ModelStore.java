package dev.capcap.mobile;
import java.io.*;
import java.security.MessageDigest;
final class ModelStore {
 private ModelStore(){}
 static synchronized File install(File directory,String name,InputStream source,long size,String sha256)throws Exception{
 if(!name.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,127}")||size<=0||!sha256.matches("[a-fA-F0-9]{64}"))throw new IOException("Invalid model manifest");
 if(!directory.isDirectory()&&!directory.mkdirs())throw new IOException("Cannot create model directory");
 File target=new File(directory,name),temp=File.createTempFile("model-",".part",directory);
 try{
 MessageDigest digest=MessageDigest.getInstance("SHA-256");long total=0;
 try(FileOutputStream out=new FileOutputStream(temp)){byte[] buffer=new byte[65536];int n;while((n=source.read(buffer))!=-1){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException();if(n==0)continue;total+=n;if(total>size)throw new IOException("Model exceeds declared size");digest.update(buffer,0,n);out.write(buffer,0,n);}out.getFD().sync();}
 StringBuilder hex=new StringBuilder();for(byte b:digest.digest())hex.append(String.format(java.util.Locale.ROOT,"%02x",b&255));
 if(total!=size||!sha256.equalsIgnoreCase(hex.toString()))throw new IOException("Model checksum mismatch");
 if(!temp.renameTo(target))throw new IOException("Cannot publish verified model");return target;
 }finally{temp.delete();}
 }
}
