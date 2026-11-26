package dev.capcap.mobile;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.*;
import android.view.ViewGroup;
import java.io.*;
import java.nio.charset.StandardCharsets;
public class MainActivity extends Activity {
 android.net.Uri selectedVideo; TextView caption; android.os.Handler handler=new android.os.Handler(android.os.Looper.getMainLooper()); java.util.List<Srt.Cue> cues=new java.util.ArrayList<>(); VideoView video; EditText subtitles; android.speech.tts.TextToSpeech tts; boolean voiceReady;
 @Override public void onCreate(Bundle b){super.onCreate(b);
 LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,48,24,24);
 TextView title=new TextView(this); title.setText("Capcut Free • Android"); title.setTextSize(24); root.addView(title);
 video=new VideoView(this); root.addView(video,new LinearLayout.LayoutParams(-1,0,2));
 MediaController controls=new MediaController(this); video.setMediaController(controls);
 caption=new TextView(this); caption.setTextSize(20); root.addView(caption);
 button(root,"Chọn video",()->pick("video/*",1));
 button(root,"Nhập phụ đề SRT",()->pick("*/*",2));
 subtitles=new EditText(this); subtitles.setGravity(android.view.Gravity.TOP); subtitles.setHint("Nhập hoặc chỉnh sửa phụ đề SRT"); root.addView(subtitles,new LinearLayout.LayoutParams(-1,0,2));
 button(root,"Áp dụng phụ đề vào xem trước",()->{try{cues=Srt.parse(subtitles.getText().toString());Toast.makeText(this,"Đã nạp "+cues.size()+" câu",Toast.LENGTH_SHORT).show();}catch(IllegalArgumentException e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}});
 button(root,"Lưu phụ đề SRT",()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/x-subrip");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"subtitles.srt");startActivityForResult(i,3);});
 button(root,"Đọc phụ đề tiếng Việt",()->{if(!voiceReady){Toast.makeText(this,"Giọng tiếng Việt chưa sẵn sàng",Toast.LENGTH_SHORT).show();return;} String text=subtitles.getText().toString().replaceAll("(?m)^\\s*\\d+\\s*$","").replaceAll("(?m)^.*-->.*$","").trim(); if(!text.isEmpty())tts.speak(text,android.speech.tts.TextToSpeech.QUEUE_FLUSH,null,"preview");});
 button(root,"Dừng đọc",()->{if(tts!=null)tts.stop();});
 setContentView(root); handler.post(tick);
 tts=new android.speech.tts.TextToSpeech(this,status->{if(status==android.speech.tts.TextToSpeech.SUCCESS && tts!=null){voiceReady=tts.setLanguage(new java.util.Locale("vi","VN"))>=0;}});
 subtitles.setText(getPreferences(MODE_PRIVATE).getString("draft","")); String saved=getPreferences(MODE_PRIVATE).getString("video",""); if(!saved.isEmpty()){selectedVideo=android.net.Uri.parse(saved);video.setVideoURI(selectedVideo);}
 }
 final Runnable tick=new Runnable(){public void run(){caption.setText(Srt.at(cues,video.getCurrentPosition()));handler.postDelayed(this,150);}};
 @Override protected void onDestroy(){handler.removeCallbacks(tick);if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
 void button(LinearLayout root,String label,Runnable action){Button b=new Button(this);b.setText(label);b.setOnClickListener(v->action.run());root.addView(b);}
 void pick(String mime,int code){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType(mime);i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,code);}
 @Override protected void onPause(){super.onPause();getPreferences(MODE_PRIVATE).edit().putString("draft",subtitles.getText().toString()).apply();if(video.isPlaying())video.pause();if(tts!=null)tts.stop();}
 @Override protected void onActivityResult(int code,int result,Intent data){super.onActivityResult(code,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;
 try{if(code==1){selectedVideo=data.getData();getContentResolver().takePersistableUriPermission(selectedVideo,Intent.FLAG_GRANT_READ_URI_PERMISSION);getPreferences(MODE_PRIVATE).edit().putString("video",selectedVideo.toString()).apply();video.setVideoURI(selectedVideo);video.start();}
 else if(code==2){try(InputStream in=getContentResolver().openInputStream(data.getData());ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){if(out.size()+n>2000000)throw new IOException("Tệp quá lớn");out.write(buf,0,n);}subtitles.setText(new String(out.toByteArray(),StandardCharsets.UTF_8));}}
 else if(code==3){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){out.write(subtitles.getText().toString().getBytes(StandardCharsets.UTF_8));}Toast.makeText(this,"Đã lưu phụ đề",Toast.LENGTH_SHORT).show();}
 }catch(Exception e){Toast.makeText(this,"Không thể xử lý tệp: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
}
