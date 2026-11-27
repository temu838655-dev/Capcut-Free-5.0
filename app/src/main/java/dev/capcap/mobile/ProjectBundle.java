package dev.capcap.mobile;
import android.content.Context;
import android.net.Uri;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
final class ProjectBundle {
 static void write(Context c,Uri source,Uri target,String srt)throws IOException{
 Srt.parse(srt);String mime=c.getContentResolver().getType(source);String extension=mime==null?null:android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);if(extension==null||!extension.matches("[a-zA-Z0-9]{1,10}"))extension="bin";
 String video="video."+extension;
 try(InputStream in=c.getContentResolver().openInputStream(source);OutputStream out=c.getContentResolver().openOutputStream(target,"wt")){
 if(in==null||out==null)throw new IOException("Không mở được tệp");
 try(ZipOutputStream zip=new ZipOutputStream(new BufferedOutputStream(out))){
 zip.setLevel(0);zip.putNextEntry(new ZipEntry(video));byte[] buf=new byte[65536];int n;while((n=in.read(buf))!=-1)zip.write(buf,0,n);zip.closeEntry();
 zip.setLevel(6);entry(zip,"subtitles.srt",srt);entry(zip,"README.txt","CapCap Android project exchange bundle\nExtract the ZIP, then import "+video+" and subtitles.srt into your video editor.\nThis is not a native CapCut draft.\n");
 }}
 }
 static void entry(ZipOutputStream zip,String name,String value)throws IOException{zip.putNextEntry(new ZipEntry(name));zip.write(value.getBytes(StandardCharsets.UTF_8));zip.closeEntry();}
}
