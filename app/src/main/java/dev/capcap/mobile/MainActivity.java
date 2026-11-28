package dev.capcap.mobile;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.*;
import android.view.ViewGroup;
import java.io.*;
import java.nio.charset.StandardCharsets;
@androidx.annotation.OptIn(markerClass=androidx.media3.common.util.UnstableApi.class)
public class MainActivity extends Activity {
 android.net.Uri selectedVideo; TextView caption; android.os.Handler handler=new android.os.Handler(android.os.Looper.getMainLooper()); java.util.List<Srt.Cue> cues=new java.util.ArrayList<>(); VideoView video; EditText subtitles; android.speech.tts.TextToSpeech tts; boolean voiceReady; boolean narrate; String lastSpoken=""; int lastPosition=-1;
 @Override public void onCreate(Bundle b){super.onCreate(b);
 LinearLayout shell=new LinearLayout(this);shell.setOrientation(LinearLayout.VERTICAL);shell.setBackgroundColor(0xff0b171b);LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18),dp(24),dp(18),dp(24));root.setBackgroundColor(0xff0b171b);root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(dp(18),dp(18)+insets.getSystemWindowInsetTop(),dp(18),dp(24)+insets.getSystemWindowInsetBottom());return insets;});
 TextView title=new TextView(this); title.setText("Capcut Free • Android"); title.setTextSize(30);title.setTextColor(0xffeffcfb);title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);root.addView(title);TextView tagline=new TextView(this);tagline.setText("STUDIO  /  VIDEO • AI • SUBTITLE");tagline.setTextColor(0xff9abdbd);tagline.setTextSize(12);root.addView(tagline);LinearLayout project=page(root,"01  •  DỰ ÁN");
 video=new VideoView(this); project.addView(video,new LinearLayout.LayoutParams(-1,0,2));
 MediaController controls=new MediaController(this); video.setMediaController(controls);
 caption=new TextView(this); caption.setTextSize(20);caption.setTextColor(0xffe0f9ff);caption.setGravity(android.view.Gravity.CENTER); project.addView(caption);
 button(project,"Chọn video",()->pick("video/*",1));
 button(project,"Nhập phụ đề SRT / VTT / ASS",()->pick("*/*",2));button(project,"Trích xuất âm thanh hàng loạt",()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("video/*");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);startActivityForResult(i,13);});button(project,"Lưu dự án có tên",()->saveNamed());button(project,"Mở / quản lý dự án",()->openNamed());
 LinearLayout ai=page(root,"02  •  PHỤ ĐỀ & AI");
 subtitles=new EditText(this); subtitles.setGravity(android.view.Gravity.TOP); subtitles.setHint("Nhập hoặc chỉnh sửa phụ đề SRT");subtitles.setTextColor(0xffeffcfb);subtitles.setHintTextColor(0xff9abdbd);subtitles.setBackground(panel(0xff163238,16));subtitles.setPadding(dp(14),dp(12),dp(14),dp(12)); ai.addView(subtitles,new LinearLayout.LayoutParams(-1,0,2));
 button(ai,"Tự động • nhận diện và dịch video",()->autoSubtitle(0));
 button(ai,"Tự động • nhận diện, dịch và lồng tiếng WAV",()->autoSubtitle(1));
 button(ai,"Tự động • dịch, lồng tiếng và xuất MP4",()->autoSubtitle(2));
 button(ai,"Whisper • nhận diện video đang mở",()->{if(selectedVideo==null){Toast.makeText(this,"Chọn video trước",Toast.LENGTH_SHORT).show();return;}transcribe(selectedVideo,true);});
 button(ai,"Whisper • tạo phụ đề từ tệp âm thanh",()->pick("audio/*",5));
 button(ai,"OCR phụ đề video",()->scanVideo());
 button(ai,"OCR ảnh phụ đề",()->pick("image/*",4));
 button(ai,"Dịch AI OpenAI",()->aiTranslate(false));
 button(ai,"Dịch AI Gemini",()->aiTranslate(true));
 button(ai,"Dịch máy sang tiếng Việt",()->translate());
 button(ai,"Áp dụng phụ đề vào xem trước",()->{try{cues=Srt.parse(subtitles.getText().toString());Toast.makeText(this,"Đã nạp "+cues.size()+" câu",Toast.LENGTH_SHORT).show();}catch(IllegalArgumentException e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}});
 button(ai,"Timeline • chỉnh từng câu",()->timeline());
 button(ai,"Lưu phụ đề ASS",()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("text/plain");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"subtitles.ass");startActivityForResult(i,16);});
 button(ai,"Lưu phụ đề WebVTT",()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("text/vtt");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"subtitles.vtt");startActivityForResult(i,15);});
 button(ai,"Lưu phụ đề SRT",()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/x-subrip");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"subtitles.srt");startActivityForResult(i,3);});
 LinearLayout exports=page(root,"03  •  XUẤT VIDEO & ÂM THANH");
 button(exports,"Xuất MP4 • trộn âm thanh gốc và giọng AI",()->{if(selectedVideo==null){Toast.makeText(this,"Chọn video trước",Toast.LENGTH_SHORT).show();return;}pick("audio/*",9);});
 button(exports,"Xuất MP4 • thay âm thanh WAV/AAC",()->{if(selectedVideo==null){Toast.makeText(this,"Chọn video trước",Toast.LENGTH_SHORT).show();return;}pick("audio/*",7);});
 button(exports,"Tách track âm thanh AAC từ video",()->{if(selectedVideo==null){Toast.makeText(this,"Chọn video trước",Toast.LENGTH_SHORT).show();return;}Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("audio/mp4");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"original.m4a");startActivityForResult(i,12);});
 button(exports,"Xuất gói chuyển dự án (ZIP)",()->{try{if(selectedVideo==null)throw new IOException("Chọn video trước");Srt.parse(subtitles.getText().toString());Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/zip");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"capcap-project.zip");startActivityForResult(i,11);}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}});
 button(exports,"Xuất giọng AI WAV",()->exportVoice());
 LinearLayout speech=page(root,"04  •  THUYẾT MINH");
 button(speech,"Xuất MP4 lồng tiếng offline",()->pickOfflineMp4());
 button(speech,"Xuất WAV giọng Việt miễn phí",()->pickOfflineWav());
 button(speech,"Chọn giọng Việt offline miễn phí",()->chooseOfflineVoice());
 button(speech,"Đọc phụ đề tiếng Việt",()->{if(!voiceReady){Toast.makeText(this,"Giọng tiếng Việt chưa sẵn sàng",Toast.LENGTH_SHORT).show();return;} String text=subtitles.getText().toString().replaceAll("(?m)^\\s*\\d+\\s*$","").replaceAll("(?m)^.*-->.*$","").trim(); if(!text.isEmpty())tts.speak(text,android.speech.tts.TextToSpeech.QUEUE_FLUSH,null,"preview");});
 Switch narration=new Switch(this);narration.setText("Thuyết minh đồng bộ video");speech.addView(narration);narration.setOnCheckedChangeListener((v,on)->{narrate=on;lastSpoken="";if(!on&&tts!=null)tts.stop();});
 button(speech,"Dừng đọc",()->{narration.setChecked(false);if(tts!=null)tts.stop();});
 android.widget.ScrollView scroll=new android.widget.ScrollView(this);scroll.setFillViewport(true);video.setLayoutParams(new LinearLayout.LayoutParams(-1,(int)(220*getResources().getDisplayMetrics().density)));subtitles.setLayoutParams(new LinearLayout.LayoutParams(-1,(int)(180*getResources().getDisplayMetrics().density)));scroll.addView(root);shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);nav.setPadding(dp(6),dp(8),dp(6),dp(8));shell.addView(nav);LinearLayout[] pages={project,ai,exports,speech};String[] names={"Dự án","AI Studio","Xuất","Giọng đọc"};Button[] tabs=new Button[4];for(int i=0;i<4;i++){final int index=i;Button tab=new Button(this);tabs[i]=tab;tab.setAllCaps(false);tab.setText(names[i]);tab.setTextSize(12);tab.setPadding(0,0,0,0);nav.addView(tab,new LinearLayout.LayoutParams(0,dp(52),1));tab.setOnClickListener(v->{showPage(pages,tabs,index);scroll.scrollTo(0,0);});}showPage(pages,tabs,0);setContentView(shell); handler.post(tick);
 tts=new android.speech.tts.TextToSpeech(this,status->{if(status==android.speech.tts.TextToSpeech.SUCCESS && tts!=null){voiceReady=tts.setLanguage(new java.util.Locale("vi","VN"))>=0;restoreOfflineVoice();}});
 subtitles.setText(getPreferences(MODE_PRIVATE).getString("draft",""));try{cues=Srt.parse(subtitles.getText().toString());}catch(IllegalArgumentException ignored){} String saved=getPreferences(MODE_PRIVATE).getString("video",""); if(!saved.isEmpty()){selectedVideo=android.net.Uri.parse(saved);video.setVideoURI(selectedVideo);}
 }
 final Runnable tick=new Runnable(){public void run(){int position=video.getCurrentPosition();String current=Srt.at(cues,position);caption.setText(current);
 if(lastPosition>=0&&Math.abs(position-lastPosition)>1000){lastSpoken="";if(narrate&&tts!=null)tts.stop();}
 if(narrate&&voiceReady&&video.isPlaying()){if(!current.equals(lastSpoken)){lastSpoken=current;if(!current.isEmpty())tts.speak(current,android.speech.tts.TextToSpeech.QUEUE_FLUSH,null,"cue-"+position);}}else if(narrate&&tts!=null){tts.stop();lastSpoken="";}
 lastPosition=position;handler.postDelayed(this,150);}};
 String offlineExportVoice;java.util.List<Srt.Cue> offlineExportCues;boolean offlineExportRunning;
 void pickOfflineMp4(){
 if(offlineExportRunning||offlineExportCues!=null||autoDubFile!=null||activeMix!=null)return;
 final android.net.Uri source=selectedVideo;if(source==null){Toast.makeText(this,"Chưa chọn video",Toast.LENGTH_LONG).show();return;}
 final String name=getPreferences(MODE_PRIVATE).getString("offlineVoice","");if(name.isEmpty()){chooseOfflineVoice();return;}
 final java.util.List<Srt.Cue> list=Srt.parse(subtitles.getText().toString());if(list.isEmpty()){Toast.makeText(this,"Chưa có phụ đề",Toast.LENGTH_LONG).show();return;}
 try{autoDubFile=File.createTempFile("offline-dub-",".wav",getCacheDir());}catch(IOException e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();return;}
 final File file=autoDubFile;autoDubSource=source;autoDubCues=list;offlineExportRunning=true;Toast.makeText(this,"Đang tạo giọng offline cho video",Toast.LENGTH_LONG).show();
 new Thread(()->{try(OfflineVoice engine=new OfflineVoice(getApplicationContext(),name)){AiVoice.renderWith(getApplicationContext(),android.net.Uri.fromFile(file),list,engine);runOnUiThread(()->{offlineExportRunning=false;if(isDestroyed()){clearAutoDub();return;}try{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("video/mp4");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"dub-offline.mp4");startActivityForResult(i,17);}catch(Exception e){clearAutoDub();Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}});}catch(Exception e){runOnUiThread(()->{offlineExportRunning=false;clearAutoDub();if(!isDestroyed())Toast.makeText(this,"Tạo giọng offline thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();});}}).start();
 }
 void pickOfflineWav(){
 if(offlineExportRunning||offlineExportCues!=null)return;
 String name=getPreferences(MODE_PRIVATE).getString("offlineVoice","");if(name.isEmpty()){chooseOfflineVoice();return;}
 java.util.List<Srt.Cue> list=Srt.parse(subtitles.getText().toString());if(list.isEmpty()){Toast.makeText(this,"Chưa có phụ đề",Toast.LENGTH_LONG).show();return;}
 offlineExportVoice=name;offlineExportCues=list;Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("audio/wav");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"dub-offline.wav");startActivityForResult(i,18);
 }
 void finishOfflineWav(android.net.Uri target){
 final String name=offlineExportVoice;final java.util.List<Srt.Cue> list=offlineExportCues;offlineExportVoice=null;offlineExportCues=null;if(target==null||name==null||list==null||offlineExportRunning)return;
 offlineExportRunning=true;Toast.makeText(this,"Đang tạo WAV offline",Toast.LENGTH_LONG).show();
 new Thread(()->{try(OfflineVoice source=new OfflineVoice(getApplicationContext(),name)){AiVoice.renderWith(getApplicationContext(),target,list,source);runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Đã xuất WAV offline",Toast.LENGTH_LONG).show();});}catch(Exception e){runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Xuất offline thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();});}finally{runOnUiThread(()->offlineExportRunning=false);}}).start();
 }
 void restoreOfflineVoice(){
 String saved=getPreferences(MODE_PRIVATE).getString("offlineVoice","");if(saved.isEmpty())return;
 voiceReady=false;java.util.Set<android.speech.tts.Voice> voices=tts.getVoices();if(voices!=null)for(android.speech.tts.Voice v:voices)if(saved.equals(v.getName())&&"vi".equals(v.getLocale().getLanguage())&&!v.isNetworkConnectionRequired()&&(v.getFeatures()==null||!v.getFeatures().contains("notInstalled"))){voiceReady=tts.setVoice(v)==android.speech.tts.TextToSpeech.SUCCESS;break;}
 }
 void chooseOfflineVoice(){
 if(tts==null){Toast.makeText(this,"Hãy cài dữ liệu giọng tiếng Việt trong cài đặt TTS Android",Toast.LENGTH_LONG).show();return;}
 java.util.List<android.speech.tts.Voice> available=new java.util.ArrayList<>();java.util.Set<android.speech.tts.Voice> installed=tts.getVoices();if(installed!=null)for(android.speech.tts.Voice v:installed)if("vi".equals(v.getLocale().getLanguage())&&!v.isNetworkConnectionRequired()&&(v.getFeatures()==null||!v.getFeatures().contains("notInstalled")))available.add(v);
 java.util.Collections.sort(available,(a,b)->Integer.compare(b.getQuality(),a.getQuality()));
 if(available.isEmpty()){new android.app.AlertDialog.Builder(this).setMessage("Chưa có giọng Việt offline. Hãy tải dữ liệu giọng từ engine TTS Android.").setPositiveButton("Cài giọng",(d,w)->{try{startActivity(new Intent(android.speech.tts.TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA));}catch(Exception e){Toast.makeText(this,"Mở cài đặt TTS Android thủ công",Toast.LENGTH_LONG).show();}}).setNegativeButton("Đóng",null).show();return;}
 String[] labels=new String[available.size()];for(int i=0;i<labels.length;i++)labels[i]=available.get(i).getName()+" • "+available.get(i).getLocale().toString();
 new android.app.AlertDialog.Builder(this).setTitle("Giọng Việt offline • không cần API").setItems(labels,(d,w)->{if(tts.setVoice(available.get(w))==android.speech.tts.TextToSpeech.SUCCESS){voiceReady=true;getPreferences(MODE_PRIVATE).edit().putString("offlineVoice",available.get(w).getName()).apply();tts.speak("Xin chào! Đây là giọng tiếng Việt miễn phí.",android.speech.tts.TextToSpeech.QUEUE_FLUSH,null,"offline-preview");}}).setNegativeButton("Đóng",null).show();
 }
 @Override protected void onDestroy(){stopVoicePreview();handler.removeCallbacks(tick);if(activeMix!=null)activeMix.cancel();if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
 java.util.List<android.net.Uri> batchSources;
 void batchPick(Intent data){java.util.ArrayList<android.net.Uri> list=new java.util.ArrayList<>();android.content.ClipData clip=data.getClipData();if(clip!=null){for(int i=0;i<clip.getItemCount();i++){android.net.Uri u=clip.getItemAt(i).getUri();if(u!=null&&!list.contains(u))list.add(u);}}else if(data.getData()!=null)list.add(data.getData());if(list.isEmpty())return;if(list.size()>30){Toast.makeText(this,"Tối đa 30 video mỗi lượt",Toast.LENGTH_LONG).show();return;}batchSources=list;startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),14);}
 void batchExtract(android.net.Uri folder){final java.util.List<android.net.Uri> list=batchSources;batchSources=null;if(list==null)return;android.app.ProgressDialog progress=new android.app.ProgressDialog(this);progress.setTitle("Tách âm thanh hàng loạt");progress.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);progress.setMax(list.size());java.util.concurrent.atomic.AtomicBoolean cancel=new java.util.concurrent.atomic.AtomicBoolean();progress.setCancelable(false);progress.setButton(android.content.DialogInterface.BUTTON_NEGATIVE,"Dừng",(d,w)->cancel.set(true));progress.show();
 new Thread(()->{int ok=0,failed=0;StringBuilder errors=new StringBuilder();for(int i=0;i<list.size()&&!cancel.get();i++){File temp=null;android.net.Uri output=null;try{temp=AudioExtract.extract(this,list.get(i),Long.MAX_VALUE);android.net.Uri parent=android.provider.DocumentsContract.buildDocumentUriUsingTree(folder,android.provider.DocumentsContract.getTreeDocumentId(folder));output=android.provider.DocumentsContract.createDocument(getContentResolver(),parent,"audio/mp4","audio-"+System.currentTimeMillis()+"-"+(i+1)+".m4a");if(output==null)throw new IOException("Không tạo được tệp");try(InputStream in=new FileInputStream(temp);OutputStream out=getContentResolver().openOutputStream(output,"wt")){if(out==null)throw new IOException("Không mở được tệp");byte[] b=new byte[65536];int n;while((n=in.read(b))!=-1){if(cancel.get())throw new IOException("Đã dừng");out.write(b,0,n);}}ok++;}catch(Exception e){failed++;errors.append("Video ").append(i+1).append(": ").append(e.getMessage()).append('\n');if(output!=null)try{android.provider.DocumentsContract.deleteDocument(getContentResolver(),output);}catch(Exception ignored){}}finally{if(temp!=null)temp.delete();}final int done=i+1;runOnUiThread(()->{if(!isDestroyed())progress.setProgress(done);});}final String result="Thành công: "+ok+" / "+list.size()+"\nLỗi: "+failed+(cancel.get()?"\nĐã dừng":"")+"\n"+errors;runOnUiThread(()->{if(!isDestroyed()){progress.dismiss();new android.app.AlertDialog.Builder(this).setTitle("Kết quả hàng loạt").setMessage(result).setPositiveButton("Đóng",null).show();}});}).start();}
 void saveNamed(){
  EditText name=new EditText(this);
  name.setHint("Tên dự án");
  name.setSingleLine(true);
  new android.app.AlertDialog.Builder(this).setTitle("Lưu dự án").setView(name)
   .setNegativeButton("Hủy",null).setPositiveButton("Lưu",(d,w)->{
    try{
     String projectName=name.getText().toString().trim();
     if(projectName.isEmpty()||projectName.length()>100)
      throw new IOException("Tên dự án phải có từ 1 đến 100 ký tự");
     String raw=subtitles.getText().toString();
     int bytes=raw.getBytes(StandardCharsets.UTF_8).length;
     if(bytes>1500000)throw new IOException("Phụ đề quá lớn để lưu an toàn. Hãy tách dự án hoặc giảm dung lượng phụ đề.");
     java.util.List<Srt.Cue> parsed=Srt.parse(raw);
     org.json.JSONObject item=new org.json.JSONObject();
     item.put("schemaVersion",2);
     item.put("updatedAt",System.currentTimeMillis());
     item.put("cueCount",parsed.size());
     item.put("video",selectedVideo==null?"":selectedVideo.toString());
     item.put("srt",raw);
     getSharedPreferences("projects",MODE_PRIVATE).edit().putString(projectName,item.toString()).apply();
     Toast.makeText(this,"Đã lưu dự án • "+parsed.size()+" câu phụ đề",Toast.LENGTH_SHORT).show();
    }catch(Exception e){Toast.makeText(this,"Không thể lưu dự án: "+e.getMessage(),Toast.LENGTH_LONG).show();}
   }).show();
 }

 void openNamed(){
  android.content.SharedPreferences store=getSharedPreferences("projects",MODE_PRIVATE);
  String[] names=store.getAll().keySet().toArray(new String[0]);
  java.util.Arrays.sort(names,String.CASE_INSENSITIVE_ORDER);
  if(names.length==0){Toast.makeText(this,"Chưa có dự án đã lưu",Toast.LENGTH_SHORT).show();return;}
  String[] labels=new String[names.length];
  for(int i=0;i<names.length;i++){
   try{
    org.json.JSONObject item=new org.json.JSONObject(store.getString(names[i],""));
    int count=item.optInt("cueCount",-1);
    if(count<0)count=Srt.parse(item.optString("srt","")).size();
    long updated=item.optLong("updatedAt",0);
    String date=updated>0?new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm",java.util.Locale.getDefault()).format(new java.util.Date(updated)):"Bản cũ";
    labels[i]=names[i]+"  •  "+count+" câu  •  "+date;
   }catch(Exception ignored){labels[i]=names[i]+"  •  Dữ liệu cần kiểm tra";}
  }
  new android.app.AlertDialog.Builder(this).setTitle("Dự án đã lưu").setItems(labels,(d,index)->{
   final String projectName=names[index];
   new android.app.AlertDialog.Builder(this).setTitle(projectName)
    .setMessage("Bạn muốn mở hay xóa dự án này?")
    .setNegativeButton("Đóng",null)
    .setNeutralButton("Xóa",(q,k)->new android.app.AlertDialog.Builder(this)
     .setMessage("Xóa vĩnh viễn dự án "+projectName+"?")
     .setNegativeButton("Hủy",null)
     .setPositiveButton("Xóa",(z,j)->{
      store.edit().remove(projectName).apply();
      Toast.makeText(this,"Đã xóa dự án",Toast.LENGTH_SHORT).show();
     }).show())
    .setPositiveButton("Mở",(q,k)->{
     try{
      String stored=store.getString(projectName,null);
      if(stored==null)throw new IOException("Dự án không còn tồn tại");
      org.json.JSONObject item=new org.json.JSONObject(stored);
      String raw=item.getString("srt");
      if(raw.getBytes(StandardCharsets.UTF_8).length>1500000)
       throw new IOException("Dữ liệu dự án vượt giới hạn an toàn");
      java.util.List<Srt.Cue> parsed=Srt.parse(raw);
      String uriText=item.optString("video","");
      android.net.Uri restored=uriText.isEmpty()?null:android.net.Uri.parse(uriText);
      boolean videoPermission=true;
      if(restored!=null&&"content".equals(restored.getScheme())){
       videoPermission=false;
       for(android.content.UriPermission permission:getContentResolver().getPersistedUriPermissions()){
        if(restored.equals(permission.getUri())&&permission.isReadPermission()){videoPermission=true;break;}
       }
      }
      video.stopPlayback();
      lastSpoken="";
      lastPosition=-1;
      selectedVideo=restored;
      if(selectedVideo!=null&&videoPermission)video.setVideoURI(selectedVideo);
      subtitles.setText(raw);
      cues=parsed;
      getPreferences(MODE_PRIVATE).edit().putString("draft",raw).putString("video",uriText).apply();
      if(selectedVideo!=null&&!videoPermission)
       Toast.makeText(this,"Đã mở phụ đề. Hãy chọn lại video vì quyền truy cập tệp đã hết hạn.",Toast.LENGTH_LONG).show();
      else Toast.makeText(this,"Đã mở dự án • "+parsed.size()+" câu phụ đề",Toast.LENGTH_SHORT).show();
     }catch(Exception e){Toast.makeText(this,"Không thể mở dự án: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }).show();
  }).setNegativeButton("Đóng",null).show();
 }
 void timeline(){
 final java.util.List<Srt.Cue> list;try{list=Srt.parse(subtitles.getText().toString());}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();return;}
  String[] rows=new String[list.size()];for(int i=0;i<rows.length;i++){Srt.Cue c=list.get(i);rows[i]=Srt.format(c.start)+" → "+Srt.format(c.end)+"\n"+c.text;}
 android.widget.ScrollView timelineScroll=new android.widget.ScrollView(this);LinearLayout tracks=new LinearLayout(this);tracks.setOrientation(LinearLayout.VERTICAL);tracks.setPadding(dp(12),dp(8),dp(12),dp(8));timelineScroll.addView(tracks);
 long duration=Math.max(1,video.getDuration());for(Srt.Cue c:list)duration=Math.max(duration,c.end);final long span=duration;
 android.app.AlertDialog timelineDialog=new android.app.AlertDialog.Builder(this).setTitle("Timeline • "+rows.length+" câu").setView(timelineScroll).setNegativeButton("Đóng",null).setPositiveButton("Thêm câu",(d,w)->addTimelineCue(list)).create();
 for(int i=0;i<list.size();i++){final int index=i;Srt.Cue cue=list.get(i);TextView label=new TextView(this);label.setText(rows[i]);label.setPadding(dp(4),dp(8),dp(4),dp(4));tracks.addView(label);android.view.View lane=new android.view.View(this){final android.graphics.Paint paint=new android.graphics.Paint(3);@Override protected void onDraw(android.graphics.Canvas canvas){super.onDraw(canvas);float w=getWidth(),h=getHeight();paint.setColor(0xff25464a);canvas.drawRoundRect(0,0,w,h,dp(5),dp(5),paint);paint.setColor(0xff16b8a6);float left=(float)((double)cue.start/span*w),right=(float)((double)cue.end/span*w);canvas.drawRoundRect(left,0,Math.min(w,Math.max(left+dp(3),right)),h,dp(5),dp(5),paint);if(right-left>=dp(32)){paint.setColor(0xffeffcfb);canvas.drawRoundRect(left+dp(4),dp(5),left+dp(7),h-dp(5),dp(2),dp(2),paint);canvas.drawRoundRect(right-dp(7),dp(5),right-dp(4),h-dp(5),dp(2),dp(2),paint);}}};tracks.addView(lane,new LinearLayout.LayoutParams(-1,dp(24)));android.view.View.OnClickListener edit=v->{timelineDialog.dismiss();editCue(list,index);};lane.setOnClickListener(edit);lane.setOnTouchListener(new android.view.View.OnTouchListener(){float origin;boolean dragging;int edge;public boolean onTouch(android.view.View v,android.view.MotionEvent event){switch(event.getActionMasked()){case android.view.MotionEvent.ACTION_DOWN:origin=event.getX();dragging=false;float left=(float)((double)cue.start/span*v.getWidth()),right=(float)((double)cue.end/span*v.getWidth());edge=right-left<dp(32)?0:Math.abs(origin-left)<=dp(12)?-1:Math.abs(origin-right)<=dp(12)?1:0;v.getParent().requestDisallowInterceptTouchEvent(true);return true;case android.view.MotionEvent.ACTION_MOVE:if(Math.abs(event.getX()-origin)>dp(8))dragging=true;if(dragging){long shift=Math.round((event.getX()-origin)*span/Math.max(1.0,v.getWidth()));long limit=Math.min(Integer.MAX_VALUE,span);long a=cue.start,b=cue.end;if(edge==-1)a=Math.max(0,Math.min(b-1,a+shift));else if(edge==1)b=Math.max(a+1,Math.min(limit,b+shift));else{a=Math.max(0,Math.min(limit-(b-a),a+shift));b=a+cue.end-cue.start;}label.setText(Srt.format(a)+" → "+Srt.format(b)+"\n"+cue.text);}return true;case android.view.MotionEvent.ACTION_CANCEL:label.setText(rows[index]);v.getParent().requestDisallowInterceptTouchEvent(false);return true;case android.view.MotionEvent.ACTION_UP:v.getParent().requestDisallowInterceptTouchEvent(false);if(!dragging){v.performClick();return true;}try{long length=cue.end-cue.start;long delta=Math.round((event.getX()-origin)*span/Math.max(1.0,v.getWidth()));long limit=Math.min(Integer.MAX_VALUE,span);long start=Math.max(0,Math.min(limit-length,cue.start+delta));long end=start+length;if(edge==-1){start=Math.max(0,Math.min(cue.end-1,cue.start+delta));end=cue.end;}else if(edge==1){start=cue.start;end=Math.max(start+1,Math.min(limit,cue.end+delta));}if(end>limit||end<=start||length<=0)throw new IllegalArgumentException("Thời gian không hợp lệ");java.util.List<Srt.Cue> moved=Srt.parse("1\n"+Srt.format(start)+" --> "+Srt.format(end)+"\n"+cue.text+"\n\n");if(moved.size()!=1)throw new IllegalArgumentException("Phụ đề không hợp lệ");list.set(index,moved.get(0));saveTimeline(list);timelineDialog.dismiss();timeline();}catch(Exception e){Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();}return true;default:return true;}}});label.setOnClickListener(edit);lane.setContentDescription("Chỉnh phụ đề "+(i+1));}
 timelineDialog.show();
 }
 void addTimelineCue(java.util.List<Srt.Cue> list){
 long start=Math.max(0,video.getCurrentPosition());long end=Math.min(Integer.MAX_VALUE,start+2000);if(end<=start)return;
 EditText body=new EditText(this);body.setHint("Nội dung phụ đề mới");
 android.app.AlertDialog dialog=new android.app.AlertDialog.Builder(this).setTitle("Thêm phụ đề tại "+Srt.format(start)).setView(body).setNegativeButton("Hủy",null).setPositiveButton("Thêm",null).create();
 dialog.setOnShowListener(d->dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener(v->{try{String value=body.getText().toString().trim();if(value.isEmpty())throw new IllegalArgumentException("Nhập nội dung phụ đề");java.util.List<Srt.Cue> parsed=Srt.parse("1\n"+Srt.format(start)+" --> "+Srt.format(end)+"\n"+value+"\n\n");if(parsed.size()!=1)throw new IllegalArgumentException("Chỉ nhập một câu");list.add(parsed.get(0));saveTimeline(list);dialog.dismiss();timeline();}catch(Exception e){body.setError(e.getMessage());}}));dialog.show();
 }
 void editCue(java.util.List<Srt.Cue> list,int index){
 Srt.Cue cue=list.get(index);LinearLayout fields=new LinearLayout(this);fields.setOrientation(LinearLayout.VERTICAL);
 EditText start=new EditText(this),end=new EditText(this),body=new EditText(this);start.setHint("Bắt đầu (ms)");end.setHint("Kết thúc (ms)");start.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);end.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);start.setText(Long.toString(cue.start));end.setText(Long.toString(cue.end));body.setText(cue.text);fields.addView(start);fields.addView(end);fields.addView(body);
 if(selectedVideo!=null)video.seekTo((int)Math.min(Integer.MAX_VALUE,cue.start));
 android.app.AlertDialog dialog=new android.app.AlertDialog.Builder(this).setTitle("Chỉnh câu "+(index+1)).setView(fields).setNegativeButton("Hủy",null).setNeutralButton("Xóa",(d,w)->{list.remove(index);saveTimeline(list);}).setPositiveButton("Lưu",null).create();
 dialog.setOnShowListener(d->dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener(v->{try{long s=Long.parseLong(start.getText().toString()),e=Long.parseLong(end.getText().toString());String text=body.getText().toString().trim();if(s<0||e<=s||e>Integer.MAX_VALUE||text.isEmpty())throw new IllegalArgumentException("Thời gian hoặc nội dung không hợp lệ");String raw="1\n"+Srt.format(s)+" --> "+Srt.format(e)+"\n"+text+"\n\n";java.util.List<Srt.Cue> parsed=Srt.parse(raw);if(parsed.size()!=1)throw new IllegalArgumentException("Chỉ nhập một câu");list.set(index,parsed.get(0));saveTimeline(list);dialog.dismiss();}catch(Exception ex){body.setError(ex.getMessage());}}));dialog.show();
 }
 void saveTimeline(java.util.List<Srt.Cue> list){
 java.util.Collections.sort(list,(a,b)->Long.compare(a.start,b.start));StringBuilder out=new StringBuilder();for(int i=0;i<list.size();i++){Srt.Cue c=list.get(i);out.append(i+1).append('\n').append(Srt.format(c.start)).append(" --> ").append(Srt.format(c.end)).append('\n').append(c.text).append("\n\n");}subtitles.setText(out.toString());cues=Srt.parse(out.toString());getPreferences(MODE_PRIVATE).edit().putString("draft",out.toString()).apply();
 }
 boolean autoSubtitleRunning;
 void autoSubtitle(int mode){
 if(autoSubtitleRunning||autoDubFile!=null||voiceKey!=null||activeMix!=null){Toast.makeText(this,"Đang xử lý video",Toast.LENGTH_SHORT).show();return;}
 final android.net.Uri source=selectedVideo;if(source==null){Toast.makeText(this,"Chọn video trước",Toast.LENGTH_SHORT).show();return;}
 final EditText key=new EditText(this);key.setHint("API key OpenAI");key.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
 LinearLayout options=new LinearLayout(this);options.setOrientation(LinearLayout.VERTICAL);options.addView(key);final EditText model=new EditText(this);model.setHint("Model dịch");model.setText("gpt-5-mini");options.addView(model);final Spinner voice=new Spinner(this);String[] choices={"onyx:north","nova:north","onyx:central","nova:central","onyx:south","nova:south"};voice.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Bắc • Nam","Bắc • Nữ","Trung • Nam","Trung • Nữ","Nam • Nam","Nam • Nữ"}));if(mode!=0)options.addView(voice);
 new android.app.AlertDialog.Builder(this).setTitle("Tự động xử lý • OpenAI (có thể tính phí)").setView(options).setNegativeButton("Hủy",null).setPositiveButton("Bắt đầu",(d,w)->{
 final String token=key.getText().toString().trim();key.setText("");if(token.isEmpty()||autoSubtitleRunning||autoDubFile!=null||voiceKey!=null||activeMix!=null)return;final String selectedModel=model.getText().toString().trim(),selectedVoice=choices[voice.getSelectedItemPosition()];if(selectedModel.isEmpty())return;final String snapshot=subtitles.getText().toString();autoSubtitleRunning=true;
 new Thread(()->{File audio=null;try{audio=AudioExtract.extract(this,source);String raw=Whisper.transcribe(this,android.net.Uri.fromFile(audio),token,"audio/mp4");java.util.List<Srt.Cue> original=Srt.parse(raw);if(original.isEmpty())throw new IOException("Không tìm thấy lời thoại");String translated=AiTranslator.translate(token,original,false,selectedModel);runOnUiThread(()->{if(isDestroyed())return;if(source.equals(selectedVideo)&&snapshot.equals(subtitles.getText().toString())){cues=Srt.parse(translated);subtitles.setText(translated);Toast.makeText(this,"Đã nhận diện và dịch video",Toast.LENGTH_LONG).show();if(mode==2){autoExportVideo(source,token,selectedVoice,Srt.parse(translated));}else if(mode==1){voiceCues=Srt.parse(translated);voiceKey=token;voiceName=selectedVoice;Intent output=new Intent(Intent.ACTION_CREATE_DOCUMENT);output.setType("audio/wav");output.addCategory(Intent.CATEGORY_OPENABLE);output.putExtra(Intent.EXTRA_TITLE,"dub.wav");startActivityForResult(output,6);}}else Toast.makeText(this,"Dự án đã thay đổi; không ghi đè",Toast.LENGTH_LONG).show();});}catch(Exception e){runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Xử lý tự động thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();});}finally{if(audio!=null&&!audio.delete())audio.deleteOnExit();runOnUiThread(()->autoSubtitleRunning=false);}}).start();
 }).show();
 }
 File autoDubFile;android.net.Uri autoDubSource;java.util.List<Srt.Cue> autoDubCues;
 void autoExportVideo(android.net.Uri source,String token,String selectedVoice,java.util.List<Srt.Cue> list){
 if(autoDubFile!=null||activeMix!=null){Toast.makeText(this,"Đang có tác vụ xuất video",Toast.LENGTH_LONG).show();return;}
 try{autoDubFile=File.createTempFile("auto-dub-",".wav",getCacheDir());}catch(IOException e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();return;}
 final File file=autoDubFile;autoDubSource=source;autoDubCues=list;
 new Thread(()->{try{AiVoice.render(this,android.net.Uri.fromFile(file),token,selectedVoice,list);runOnUiThread(()->{if(isDestroyed()){clearAutoDub();return;}Intent output=new Intent(Intent.ACTION_CREATE_DOCUMENT);output.setType("video/mp4");output.addCategory(Intent.CATEGORY_OPENABLE);output.putExtra(Intent.EXTRA_TITLE,"dubbed.mp4");startActivityForResult(output,17);});}catch(Exception e){runOnUiThread(()->{clearAutoDub();if(!isDestroyed())Toast.makeText(this,"Lồng tiếng thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();});}}).start();
 }
 void clearAutoDub(){if(autoDubFile!=null)autoDubFile.delete();autoDubFile=null;autoDubSource=null;autoDubCues=null;}
 void finishAutoExport(android.net.Uri target){
 if(target==null||autoDubFile==null||activeMix!=null){clearAutoDub();return;}
 try{activeMix=new MixExport(this,autoDubSource,android.net.Uri.fromFile(autoDubFile),target,0.25f,1f,0,autoDubCues,42,0xffffffff,0,true,autoDubCues,()->{activeMix=null;clearAutoDub();});activeMix.start();}catch(Exception e){activeMix=null;clearAutoDub();Toast.makeText(this,"Xuất thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();}
 }
 void transcribe(android.net.Uri uri,boolean fromVideo){
 EditText key=new EditText(this);key.setHint("API key OpenAI");key.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
 new android.app.AlertDialog.Builder(this).setTitle("Whisper • gửi âm thanh lên OpenAI, tối đa 24 MB, có thể tính phí").setView(key).setNegativeButton("Hủy",null).setPositiveButton("Tạo phụ đề",(d,w)->{
 String token=key.getText().toString().trim();key.setText("");if(token.isEmpty())return;String snapshot=subtitles.getText().toString();
 Toast.makeText(this,"Đang nhận diện lời thoại...",Toast.LENGTH_LONG).show();
 new Thread(()->{try{String result;if(fromVideo){File audio=AudioExtract.extract(this,uri);try{result=Whisper.transcribe(this,android.net.Uri.fromFile(audio),token,"audio/mp4");}finally{if(!audio.delete())audio.deleteOnExit();}}else result=Whisper.transcribe(this,uri,token);runOnUiThread(()->{if(isDestroyed())return;if(snapshot.equals(subtitles.getText().toString())){cues=Srt.parse(result);subtitles.setText(result);}else Toast.makeText(this,"Phụ đề đã thay đổi; không ghi đè",Toast.LENGTH_LONG).show();});}catch(Exception e){runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Whisper thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();});}}).start();
 }).show();
 }
 android.net.Uri muxAudio,muxVideo;
 MixExport activeMix;
 void mixExport(android.net.Uri target){
 final android.net.Uri source=muxVideo,audio=muxAudio;muxVideo=null;muxAudio=null;if(source==null||audio==null)return;
 if(activeMix!=null){Toast.makeText(this,"Đang xuất video khác",Toast.LENGTH_SHORT).show();return;}
 LinearLayout fields=new LinearLayout(this);fields.setOrientation(LinearLayout.VERTICAL);
 TextView originalLabel=new TextView(this),voiceLabel=new TextView(this);SeekBar original=new SeekBar(this),voice=new SeekBar(this);original.setMax(100);voice.setMax(100);original.setProgress(25);voice.setProgress(100);originalLabel.setText("Âm thanh gốc: 25%");voiceLabel.setText("Giọng AI: 100%");fields.addView(originalLabel);fields.addView(original);fields.addView(voiceLabel);fields.addView(voice);Spinner resolution=new Spinner(this);String[] sizes={"Gốc","720p","1080p","2160p (4K)"};resolution.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,sizes));fields.addView(resolution);CheckBox burn=new CheckBox(this);burn.setText("Ghi phụ đề vào video");fields.addView(burn);
 TextView sizeLabel=new TextView(this);sizeLabel.setText("Cỡ chữ: 42");SeekBar captionSize=new SeekBar(this);captionSize.setMax(76);captionSize.setProgress(26);captionSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int n,boolean user){sizeLabel.setText("Cỡ chữ: "+(16+n));}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});fields.addView(sizeLabel);fields.addView(captionSize);
 Spinner captionColor=new Spinner(this);captionColor.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Chữ trắng","Chữ vàng","Chữ xanh nhạt"}));fields.addView(captionColor);
 Spinner captionPosition=new Spinner(this);captionPosition.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Phía dưới","Ở giữa","Phía trên"}));fields.addView(captionPosition);
 CheckBox captionBackground=new CheckBox(this);captionBackground.setText("Nền đen bán trong suốt");captionBackground.setChecked(true);fields.addView(captionBackground);CheckBox duck=new CheckBox(this);duck.setText("Tự giảm âm gốc theo thời gian phụ đề");duck.setChecked(false);fields.addView(duck);
 original.setOnSeekBarChangeListener(gainLabel(originalLabel,"Âm thanh gốc"));voice.setOnSeekBarChangeListener(gainLabel(voiceLabel,"Giọng AI"));
 android.widget.ScrollView exportOptions=new android.widget.ScrollView(this);exportOptions.addView(fields);
 new android.app.AlertDialog.Builder(this).setTitle("Âm lượng video").setView(exportOptions).setNegativeButton("Hủy",null).setPositiveButton("Xuất",(d,w)->{try{activeMix=new MixExport(this,source,audio,target,original.getProgress()/100f,voice.getProgress()/100f,new int[]{0,720,1080,2160}[resolution.getSelectedItemPosition()],burn.isChecked()?Srt.parse(subtitles.getText().toString()):java.util.Collections.emptyList(),16+captionSize.getProgress(),new int[]{0xffffffff,0xffffff00,0xff80ffff}[captionColor.getSelectedItemPosition()],captionPosition.getSelectedItemPosition(),captionBackground.isChecked(),duck.isChecked()?Srt.parse(subtitles.getText().toString()):java.util.Collections.emptyList(),()->activeMix=null);activeMix.start();}catch(Exception e){activeMix=null;Toast.makeText(this,"Không thể trộn: "+e.getMessage(),Toast.LENGTH_LONG).show();}}).show();
 }
 SeekBar.OnSeekBarChangeListener gainLabel(TextView label,String name){return new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar bar,int progress,boolean user){label.setText(name+": "+progress+"%");}public void onStartTrackingTouch(SeekBar bar){}public void onStopTrackingTouch(SeekBar bar){}};}
 void extractSound(android.net.Uri target){
 final android.net.Uri source=selectedVideo;if(source==null)return;
 new Thread(()->{File temp=null;try{temp=AudioExtract.extract(this,source,Long.MAX_VALUE);try(InputStream in=new FileInputStream(temp);OutputStream out=getContentResolver().openOutputStream(target,"wt")){if(out==null)throw new IOException("Không mở được tệp xuất");byte[] buf=new byte[65536];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);}runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Đã xuất âm thanh gốc",Toast.LENGTH_LONG).show();});}catch(Exception e){runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Tách âm thanh thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();});}finally{if(temp!=null&&!temp.delete())temp.deleteOnExit();}}).start();
 }
 void exportProject(android.net.Uri target){
 final android.net.Uri source=selectedVideo;final String srt=subtitles.getText().toString();if(source==null)return;
 Toast.makeText(this,"Đang đóng gói dự án...",Toast.LENGTH_LONG).show();
 new Thread(()->{try{ProjectBundle.write(this,source,target,srt);runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Đã lưu gói dự án ZIP",Toast.LENGTH_LONG).show();});}catch(Exception e){runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Xuất gói thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();});}}).start();
 }
 void exportMux(android.net.Uri target){
 final android.net.Uri source=muxVideo,audio=muxAudio;muxVideo=null;muxAudio=null;if(source==null||audio==null)return;
 Toast.makeText(this,"Đang xuất MP4...",Toast.LENGTH_LONG).show();
 new Thread(()->{try{File encoded=null;try{if(WavAac.isWav(this,audio)){encoded=WavAac.encode(this,audio);VideoMux.replaceAudio(this,source,android.net.Uri.fromFile(encoded),target);}else VideoMux.replaceAudio(this,source,audio,target);}finally{if(encoded!=null&&!encoded.delete())encoded.deleteOnExit();}runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Đã xuất MP4",Toast.LENGTH_LONG).show();});}catch(Exception e){runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Xuất MP4 thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();});}}).start();
 }
 String voiceKey,voiceName;java.util.List<Srt.Cue> voiceCues;
 void exportVoice(){
 EditText key=new EditText(this);key.setHint("API key OpenAI");key.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
 String[] voices={"onyx:north","nova:north","onyx:central","nova:central","onyx:south","nova:south"};Spinner spinner=new Spinner(this);spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Miền Bắc • Nam","Miền Bắc • Nữ","Miền Trung • Nam","Miền Trung • Nữ","Miền Nam • Nam","Miền Nam • Nữ"}));
 LinearLayout fields=new LinearLayout(this);fields.setOrientation(LinearLayout.VERTICAL);fields.addView(key);fields.addView(spinner);button(fields,"Nghe thử giọng (có thể tính phí)",()->previewAiVoice(key.getText().toString().trim(),voices[spinner.getSelectedItemPosition()]));
 new android.app.AlertDialog.Builder(this).setTitle("Giọng AI • gửi phụ đề đến OpenAI (có thể tính phí)").setView(fields).setNegativeButton("Hủy",null).setPositiveButton("Xuất WAV",(d,w)->{try{voiceCues=Srt.parse(subtitles.getText().toString());if(voiceCues.isEmpty())throw new IOException("Chưa có phụ đề");voiceKey=key.getText().toString().trim();key.setText("");if(voiceKey.isEmpty())return;voiceName=voices[spinner.getSelectedItemPosition()];Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("audio/wav");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"dub.wav");startActivityForResult(i,6);}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}}).show();
 }
 android.media.MediaPlayer voicePreview;File voicePreviewFile;boolean previewLoading;
 void stopVoicePreview(){if(voicePreview!=null){voicePreview.release();voicePreview=null;}if(voicePreviewFile!=null){voicePreviewFile.delete();voicePreviewFile=null;}}
 void previewAiVoice(String key,String voice){
 if(key.isEmpty()||previewLoading)return;stopVoicePreview();previewLoading=true;
 new Thread(()->{File file=null;try{byte[] pcm=AiVoice.speech(key,voice,"Xin chào! Hôm nay trời thật đẹp. Chúng ta cùng bắt đầu nhé!");file=File.createTempFile("voice-preview-",".wav",getCacheDir());try(RandomAccessFile out=new RandomAccessFile(file,"rw")){out.writeBytes("RIFF");AiVoice.le(out,36L+pcm.length,4);out.writeBytes("WAVEfmt ");AiVoice.le(out,16,4);AiVoice.le(out,1,2);AiVoice.le(out,1,2);AiVoice.le(out,AiVoice.RATE,4);AiVoice.le(out,AiVoice.RATE*2,4);AiVoice.le(out,2,2);AiVoice.le(out,16,2);out.writeBytes("data");AiVoice.le(out,pcm.length,4);out.write(pcm);}final File ready=file;runOnUiThread(()->{previewLoading=false;if(isDestroyed()){ready.delete();return;}try{voicePreviewFile=ready;voicePreview=new android.media.MediaPlayer();voicePreview.setDataSource(ready.getAbsolutePath());voicePreview.setOnCompletionListener(player->stopVoicePreview());voicePreview.setOnErrorListener((player,what,extra)->{stopVoicePreview();return true;});voicePreview.setOnPreparedListener(player->player.start());voicePreview.prepareAsync();}catch(Exception e){stopVoicePreview();Toast.makeText(this,"Không phát được giọng thử",Toast.LENGTH_LONG).show();}});}catch(Exception e){if(file!=null)file.delete();runOnUiThread(()->{previewLoading=false;if(!isDestroyed())Toast.makeText(this,"Nghe thử thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();});}}).start();
 }
 void renderVoice(android.net.Uri uri){
 String key=voiceKey,voice=voiceName;java.util.List<Srt.Cue> list=voiceCues;voiceKey=null;voiceCues=null;if(key==null||list==null)return;
 Toast.makeText(this,"Đang tạo giọng AI...",Toast.LENGTH_LONG).show();
 new Thread(()->{try{AiVoice.render(this,uri,key,voice,list);runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Đã xuất WAV",Toast.LENGTH_LONG).show();});}catch(Exception e){runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Xuất giọng thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();});}}).start();
 }
 void scanVideo(){
 String[] languages={"Latin (Việt, Anh...)","Tiếng Trung","Tiếng Nhật","Tiếng Hàn"};
 new android.app.AlertDialog.Builder(this).setTitle("Ngôn ngữ OCR").setItems(languages,(d,language)->new android.app.AlertDialog.Builder(this).setTitle("Vùng quét").setItems(new String[]{"40% phía dưới","Toàn bộ khung hình","40% phía trên"},(dialog,region)->scanVideo(language,region)).show()).show();
 }
 void scanVideo(int language,int region){
 if(selectedVideo==null){Toast.makeText(this,"Chọn video trước",Toast.LENGTH_SHORT).show();return;}
 final android.net.Uri uri=selectedVideo;final String snapshot=subtitles.getText().toString();
 java.util.concurrent.atomic.AtomicBoolean cancel=new java.util.concurrent.atomic.AtomicBoolean(false);
 android.app.ProgressDialog progress=new android.app.ProgressDialog(this);progress.setTitle("OCR video");progress.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);progress.setMax(100);progress.setCancelable(false);progress.setButton(android.content.DialogInterface.BUTTON_NEGATIVE,"Hủy",(d,w)->cancel.set(true));progress.show();
 new Thread(()->{try{String result=VideoOcr.scan(this,uri,language,region,cancel,n->runOnUiThread(()->{if(!isDestroyed())progress.setProgress(n);}));runOnUiThread(()->{if(isDestroyed())return;progress.dismiss();if(cancel.get())return;if(snapshot.equals(subtitles.getText().toString())&&uri.equals(selectedVideo)){cues=Srt.parse(result);subtitles.setText(result);}else Toast.makeText(this,"Dự án đã thay đổi; không ghi đè",Toast.LENGTH_LONG).show();});}catch(Exception e){runOnUiThread(()->{if(isDestroyed())return;progress.dismiss();if(!cancel.get())Toast.makeText(this,"OCR thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();});}}).start();
 }
 void recognizeImage(android.net.Uri uri){
 try{
 com.google.mlkit.vision.common.InputImage image=com.google.mlkit.vision.common.InputImage.fromFilePath(this,uri);
 com.google.mlkit.vision.text.TextRecognizer recognizer=com.google.mlkit.vision.text.TextRecognition.getClient(com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS);
 recognizer.process(image).addOnSuccessListener(result->{if(isDestroyed())return;String text=result.getText().trim();if(text.isEmpty()){Toast.makeText(this,"Không tìm thấy chữ",Toast.LENGTH_SHORT).show();return;}new android.app.AlertDialog.Builder(this).setTitle("Văn bản OCR").setMessage(text).setNegativeButton("Đóng",null).setPositiveButton("Sao chép",(d,w)->{android.content.ClipboardManager clipboard=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);clipboard.setPrimaryClip(android.content.ClipData.newPlainText("OCR",text));}).show();}).addOnFailureListener(e->{if(!isDestroyed())Toast.makeText(this,"OCR thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();}).addOnCompleteListener(t->recognizer.close());
 }catch(IOException e){Toast.makeText(this,"Không đọc được ảnh",Toast.LENGTH_LONG).show();}
 }
 void aiTranslate(boolean gemini){
 final EditText key=new EditText(this);key.setHint(gemini?"Gemini API keys • mỗi dòng một khóa":"OpenAI API keys • mỗi dòng một khóa");key.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);key.setSingleLine(false);key.setMinLines(2);key.setMaxLines(4);key.setHorizontallyScrolling(false);
 final EditText model=new EditText(this);model.setSingleLine(true);model.setText(gemini?"gemini-3.5-flash-lite":"gpt-5-mini");LinearLayout fields=new LinearLayout(this);fields.setOrientation(LinearLayout.VERTICAL);fields.addView(key);fields.addView(model);
 final EditText backupKey=new EditText(this);backupKey.setHint(gemini?"OpenAI dự phòng • mỗi dòng một khóa (tùy chọn)":"Gemini dự phòng • mỗi dòng một khóa (tùy chọn)");backupKey.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);backupKey.setSingleLine(false);backupKey.setMinLines(2);backupKey.setMaxLines(4);backupKey.setHorizontallyScrolling(false);fields.addView(backupKey);final EditText backupModel=new EditText(this);backupModel.setSingleLine(true);backupModel.setText(gemini?"gpt-5-mini":"gemini-3.5-flash-lite");fields.addView(backupModel);
 new android.app.AlertDialog.Builder(this).setTitle("Dịch AI • gửi phụ đề tới "+(gemini?"Gemini":"OpenAI")+" (có thể tính phí; nếu nhập key dự phòng, phụ đề có thể gửi tới cả hai)").setView(fields).setNegativeButton("Hủy",null).setPositiveButton("Dịch",(d,w)->{
 String selectedModel=model.getText().toString().trim();if(!selectedModel.matches("[A-Za-z0-9._-]{1,100}")){Toast.makeText(this,"Tên model không hợp lệ",Toast.LENGTH_SHORT).show();return;} String token=key.getText().toString().trim();key.setText("");if(token.isEmpty())return;
 final String fallbackToken=backupKey.getText().toString().trim(),fallbackModel=backupModel.getText().toString().trim();backupKey.setText("");if(!fallbackToken.isEmpty()&&!fallbackModel.matches("[A-Za-z0-9._-]{1,100}")){Toast.makeText(this,"Model dự phòng không hợp lệ",Toast.LENGTH_SHORT).show();return;}
 String snapshot=subtitles.getText().toString();final java.util.List<Srt.Cue> original;try{original=Srt.parse(snapshot);}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();return;}
 if(original.isEmpty())return;
 Toast.makeText(this,"Đang dịch AI...",Toast.LENGTH_LONG).show();
 new Thread(()->{try{final String result=AiTranslator.translate(token,original,gemini,selectedModel,fallbackToken,fallbackModel);runOnUiThread(()->{if(isDestroyed())return;if(snapshot.equals(subtitles.getText().toString())){subtitles.setText(result);cues=Srt.parse(result);getPreferences(MODE_PRIVATE).edit().putString("draft",result).apply();}else Toast.makeText(this,"Phụ đề đã thay đổi; không ghi đè",Toast.LENGTH_LONG).show();});}catch(Exception e){runOnUiThread(()->{if(isDestroyed())return;if(e instanceof AiTranslator.PartialTranslationException){AiTranslator.PartialTranslationException partial=(AiTranslator.PartialTranslationException)e;if(snapshot.equals(subtitles.getText().toString())){subtitles.setText(partial.partialSrt);cues=Srt.parse(partial.partialSrt);getPreferences(MODE_PRIVATE).edit().putString("draft",partial.partialSrt).apply();Toast.makeText(this,"Đã dịch "+partial.translatedCount+"/"+partial.totalCount+" câu. Phần còn lại được giữ nguyên bản gốc để không mất phụ đề.",Toast.LENGTH_LONG).show();}else Toast.makeText(this,"Dự án đã thay đổi; bản dịch dở dang không ghi đè.",Toast.LENGTH_LONG).show();}else Toast.makeText(this,"Dịch AI thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();});}}).start();
 }).show();
 }
 void translate(){
 final EditText lang=new EditText(this);lang.setHint("en, zh, ja, ko...");
 new android.app.AlertDialog.Builder(this).setTitle("Ngôn ngữ nguồn").setView(lang).setNegativeButton("Hủy",null).setPositiveButton("Dịch",(d,w)->{
 String source=com.google.mlkit.nl.translate.TranslateLanguage.fromLanguageTag(lang.getText().toString().trim());
 if(source==null){Toast.makeText(this,"Ngôn ngữ không hỗ trợ",Toast.LENGTH_LONG).show();return;}
 final java.util.List<Srt.Cue> original;try{original=Srt.parse(subtitles.getText().toString());}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();return;}
 if(original.isEmpty())return;
 final String snapshot=subtitles.getText().toString();
 com.google.mlkit.nl.translate.Translator client=com.google.mlkit.nl.translate.Translation.getClient(new com.google.mlkit.nl.translate.TranslatorOptions.Builder().setSourceLanguage(source).setTargetLanguage(com.google.mlkit.nl.translate.TranslateLanguage.VIETNAMESE).build());
 Toast.makeText(this,"Đang tải mô hình và dịch...",Toast.LENGTH_LONG).show();
 client.downloadModelIfNeeded().addOnSuccessListener(v->translateNext(client,original,0,new StringBuilder(),snapshot)).addOnFailureListener(e->{client.close();Toast.makeText(this,"Không tải được mô hình: "+e.getMessage(),Toast.LENGTH_LONG).show();});
 }).show();
 }
 void translateNext(com.google.mlkit.nl.translate.Translator client,java.util.List<Srt.Cue> list,int i,StringBuilder out,String snapshot){
 if(isFinishing()||isDestroyed()){client.close();return;}
 if(i==list.size()){client.close();if(snapshot.equals(subtitles.getText().toString())){subtitles.setText(out.toString());cues=Srt.parse(out.toString());}else Toast.makeText(this,"Phụ đề đã thay đổi; không ghi đè",Toast.LENGTH_LONG).show();return;}
 Srt.Cue c=list.get(i);client.translate(c.text).addOnSuccessListener(t->{out.append(i+1).append('\n').append(Srt.format(c.start)).append(" --> ").append(Srt.format(c.end)).append('\n').append(t).append("\n\n");translateNext(client,list,i+1,out,snapshot);}).addOnFailureListener(e->{client.close();Toast.makeText(this,"Dịch thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();});
 }
 LinearLayout page(LinearLayout root,String label){LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);root.addView(p);section(p,label);return p;}
 void showPage(LinearLayout[] pages,Button[] tabs,int index){for(int i=0;i<pages.length;i++){pages[i].setVisibility(i==index?android.view.View.VISIBLE:android.view.View.GONE);tabs[i].setTextColor(i==index?0xff0b171b:0xff9abdbd);tabs[i].setBackground(panel(i==index?0xff64e6d5:0xff0b171b,12));}}
 int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 android.graphics.drawable.GradientDrawable panel(int color,int radius){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 void section(LinearLayout root,String label){TextView t=new TextView(this);t.setText(label);t.setTextColor(0xff64e6d5);t.setTextSize(13);t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);t.setPadding(0,dp(24),0,dp(8));root.addView(t);}
 void button(LinearLayout root,String label,Runnable action){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(15);b.setGravity(android.view.Gravity.CENTER_VERTICAL|android.view.Gravity.START);boolean primary=label.startsWith("Chọn video")||label.startsWith("Xuất MP4");b.setTextColor(primary?0xff0b171b:0xffedf1ff);b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x337fffff),panel(primary?0xff64e6d5:0xff252b40,14),null));b.setPadding(dp(16),dp(10),dp(16),dp(10));b.setMinHeight(dp(52));b.setOnClickListener(v->action.run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(8);root.addView(b,lp);}
 void pick(String mime,int code){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType(mime);i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,code);}
 @Override protected void onPause(){super.onPause();getPreferences(MODE_PRIVATE).edit().putString("draft",subtitles.getText().toString()).apply();if(video.isPlaying())video.pause();if(tts!=null)tts.stop();}
 @Override protected void onActivityResult(int code,int result,Intent data){super.onActivityResult(code,result,data);if(code==18){finishOfflineWav(result==RESULT_OK&&data!=null?data.getData():null);return;}if(code==17){finishAutoExport(result==RESULT_OK&&data!=null?data.getData():null);return;}if(code==13){if(result==RESULT_OK&&data!=null)batchPick(data);return;}if(code==14){if(result==RESULT_OK&&data!=null&&data.getData()!=null)batchExtract(data.getData());else batchSources=null;return;}if((code==8||code==10)&&(result!=RESULT_OK||data==null||data.getData()==null)){muxVideo=null;muxAudio=null;}if(code==6&&(result!=RESULT_OK||data==null||data.getData()==null)){voiceKey=null;voiceCues=null;}if(result!=RESULT_OK||data==null||data.getData()==null)return;
 try{if(code==1){lastSpoken="";lastPosition=-1;selectedVideo=data.getData();getContentResolver().takePersistableUriPermission(selectedVideo,Intent.FLAG_GRANT_READ_URI_PERMISSION);getPreferences(MODE_PRIVATE).edit().putString("video",selectedVideo.toString()).apply();video.setVideoURI(selectedVideo);video.start();}
 else if(code==2){try(InputStream in=getContentResolver().openInputStream(data.getData());ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){if(out.size()+n>2000000)throw new IOException("Tệp quá lớn");out.write(buf,0,n);}subtitles.setText(new String(out.toByteArray(),StandardCharsets.UTF_8));}}
 else if(code==12){extractSound(data.getData());}
 else if(code==11){exportProject(data.getData());}
 else if(code==9){muxVideo=selectedVideo;muxAudio=data.getData();Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("video/mp4");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"mixed.mp4");startActivityForResult(i,10);}
 else if(code==10){mixExport(data.getData());}
 else if(code==7){muxVideo=selectedVideo;muxAudio=data.getData();Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("video/mp4");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"dubbed.mp4");startActivityForResult(i,8);}
 else if(code==8){exportMux(data.getData());}
 else if(code==6){renderVoice(data.getData());}
 else if(code==5){transcribe(data.getData(),false);}
 else if(code==4){recognizeImage(data.getData());}
 else if(code==3||code==15||code==16){java.util.List<Srt.Cue> parsed=Srt.parse(subtitles.getText().toString());StringBuilder exported=new StringBuilder(code==16?"[Script Info]\nScriptType: v4.00+\nPlayResX: 1920\nPlayResY: 1080\n\n[V4+ Styles]\nFormat: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding\nStyle: Default,Arial,48,&H00FFFFFF,&H000000FF,&H00000000,&H80000000,0,0,0,0,100,100,0,0,1,2,1,2,20,20,30,1\n\n[Events]\nFormat: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n":code==15?"WEBVTT\n\n":"");int index=1;for(Srt.Cue cue:parsed){if(code==16){String a=Srt.format(cue.start/10*10).replace(',','.'),b=Srt.format(Math.max(cue.start/10*10+10,(cue.end+9)/10*10)).replace(',','.');exported.append("Dialogue: 0,").append(a.substring(0,a.length()-1)).append(',').append(b.substring(0,b.length()-1)).append(",Default,,0,0,0,,").append(cue.text.replace("\\","＼").replace("{","｛").replace("}","｝").replace("\n","\\N")).append('\n');continue;}if(code==3)exported.append(index++).append('\n');String a=Srt.format(cue.start),b=Srt.format(cue.end);exported.append(code==15?a.replace(',','.'):a).append(" --> ").append(code==15?b.replace(',','.'):b).append('\n').append(cue.text).append("\n\n");}try(OutputStream out=getContentResolver().openOutputStream(data.getData())){if(out==null)throw new IOException("Không mở được tệp");out.write(exported.toString().getBytes(StandardCharsets.UTF_8));}Toast.makeText(this,"Đã lưu phụ đề",Toast.LENGTH_SHORT).show();}
 }catch(Exception e){Toast.makeText(this,"Không thể xử lý tệp: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
}
