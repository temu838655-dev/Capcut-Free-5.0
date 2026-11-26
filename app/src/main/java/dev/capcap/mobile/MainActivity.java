package dev.capcap.mobile;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.*;
import android.view.ViewGroup;
import java.io.*;
import java.nio.charset.StandardCharsets;
public class MainActivity extends Activity {
 VideoView video; EditText subtitles;
 @Override public void onCreate(Bundle b){super.onCreate(b);
 LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setPadding(24,48,24,24);
 TextView title=new TextView(this); title.setText("Capcut Free • Android"); title.setTextSize(24); root.addView(title);
 video=new VideoView(this); root.addView(video,new LinearLayout.LayoutParams(-1,0,2));
 MediaController controls=new MediaController(this); video.setMediaController(controls);
 button(root,"Chọn video",()->pick("video/*",1));
 button(root,"Nhập phụ đề SRT",()->pick("*/*",2));
 subtitles=new EditText(this); subtitles.setGravity(48); subtitles.setHint("Nhập hoặc chỉnh sửa phụ đề SRT"); root.addView(subtitles,new LinearLayout.LayoutParams(-1,0,2));
 button(root,"Lưu phụ đề SRT",()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/x-subrip");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"subtitles.srt");startActivityForResult(i,3);});
 setContentView(root);
 if(b!=null)subtitles.setText(b.getString("subtitles",""));
 }
 void button(LinearLayout root,String label,Runnable action){Button b=new Button(this);b.setText(label);b.setOnClickListener(v->action.run());root.addView(b);}
 void pick(String mime,int code){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType(mime);i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,code);}
 @Override protected void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putString("subtitles",subtitles.getText().toString());}
 @Override protected void onActivityResult(int code,int result,Intent data){super.onActivityResult(code,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;
 try{if(code==1){video.setVideoURI(data.getData());video.start();}
 else if(code==2){try(InputStream in=getContentResolver().openInputStream(data.getData());ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){if(out.size()+n>2000000)throw new IOException("Tệp quá lớn");out.write(buf,0,n);}subtitles.setText(new String(out.toByteArray(),StandardCharsets.UTF_8));}}
 else if(code==3){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){out.write(subtitles.getText().toString().getBytes(StandardCharsets.UTF_8));}Toast.makeText(this,"Đã lưu phụ đề",Toast.LENGTH_SHORT).show();}
 }catch(Exception e){Toast.makeText(this,"Không thể xử lý tệp: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
}
