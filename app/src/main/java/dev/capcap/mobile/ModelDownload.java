package dev.capcap.mobile;
import java.io.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;
final class ModelDownload {
 private ModelDownload(){}
 static File download(File directory,String name,String address,long size,String sha256)throws Exception{
 URL url=new URL(address);
 for(int redirects=0;redirects<=5;redirects++){
 if(!"https".equalsIgnoreCase(url.getProtocol())||url.getUserInfo()!=null)throw new IOException("HTTPS required");
 if(Thread.currentThread().isInterrupted())throw new InterruptedIOException();
 HttpsURLConnection connection=(HttpsURLConnection)url.openConnection();
 try{connection.setInstanceFollowRedirects(false);connection.setConnectTimeout(15000);connection.setReadTimeout(30000);connection.setRequestProperty("Accept-Encoding","identity");
 int status=connection.getResponseCode();
 if(status==301||status==302||status==303||status==307||status==308){String location=connection.getHeaderField("Location");if(location==null)throw new IOException("Missing redirect");url=new URL(url,location);continue;}
 if(status!=200)throw new IOException("Model download HTTP "+status);
 String length=connection.getHeaderField("Content-Length");if(length!=null&&Long.parseLong(length)!=size)throw new IOException("Unexpected model size");
 try(InputStream input=new BufferedInputStream(connection.getInputStream())){return ModelStore.install(directory,name,input,size,sha256);}
 }finally{connection.disconnect();}
 }throw new IOException("Too many redirects");
 }
}
