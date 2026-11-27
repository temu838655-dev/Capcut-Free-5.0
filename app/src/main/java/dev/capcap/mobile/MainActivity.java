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
 LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,48,24,24);
 TextView title=new TextView(this); title.setText("Capcut Free • Android"); title.setTextSize(24); root.addView(title);
 video=new VideoView(this); root.addView(video,new LinearLayout.LayoutParams(-1,0,2));
 MediaController controls=new MediaController(this); video.setMediaController(controls);
 caption=new TextView(this); caption.setTextSize(20); root.addView(caption);
 button(root,"Chọn video",()->pick("video/*",1));
 button(root,"Nhập phụ đề SRT",()->pick("*/*",2));
 subtitles=new EditText(this); subtitles.setGravity(android.view.Gravity.TOP); subtitles.setHint("Nhập hoặc chỉnh sửa phụ đề SRT"); root.addView(subtitles,new LinearLayout.LayoutParams(-1,0,2));
 button(root,"Whisper • nhận diện video đang mở",()->{if(selectedVideo==null){Toast.makeText(this,"Chọn video trước",Toast.LENGTH_SHORT).show();return;}transcribe(selectedVideo,true);});
 button(root,"Whisper • tạo phụ đề từ tệp âm thanh",()->pick("audio/*",5));
 button(root,"OCR phụ đề video",()->scanVideo());
 button(root,"OCR ảnh phụ đề",()->pick("image/*",4));
 button(root,"Dịch AI OpenAI",()->aiTranslate(false));
 button(root,"Dịch AI Gemini",()->aiTranslate(true));
 button(root,"Dịch máy sang tiếng Việt",()->translate());
 button(root,"Áp dụng phụ đề vào xem trước",()->{try{cues=Srt.parse(subtitles.getText().toString());Toast.makeText(this,"Đã nạp "+cues.size()+" câu",Toast.LENGTH_SHORT).show();}catch(IllegalArgumentException e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}});
 button(root,"Timeline • chỉnh từng câu",()->timeline());
 button(root,"Lưu phụ đề SRT",()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/x-subrip");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"subtitles.srt");startActivityForResult(i,3);});
 button(root,"Xuất MP4 • trộn âm thanh gốc và giọng AI",()->{if(selectedVideo==null){Toast.makeText(this,"Chọn video trước",Toast.LENGTH_SHORT).show();return;}pick("audio/*",9);});
 button(root,"Xuất MP4 • thay âm thanh WAV/AAC",()->{if(selectedVideo==null){Toast.makeText(this,"Chọn video trước",Toast.LENGTH_SHORT).show();return;}pick("audio/*",7);});
 button(root,"Tách track âm thanh AAC từ video",()->{if(selectedVideo==null){Toast.makeText(this,"Chọn video trước",Toast.LENGTH_SHORT).show();return;}Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("audio/mp4");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"original.m4a");startActivityForResult(i,12);});
 button(root,"Xuất gói chuyển dự án (ZIP)",()->{try{if(selectedVideo==null)throw new IOException("Chọn video trước");Srt.parse(subtitles.getText().toString());Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/zip");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"capcap-project.zip");startActivityForResult(i,11);}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}});
 button(root,"Xuất giọng AI WAV",()->exportVoice());
 button(root,"Đọc phụ đề tiếng Việt",()->{if(!voiceReady){Toast.makeText(this,"Giọng tiếng Việt chưa sẵn sàng",Toast.LENGTH_SHORT).show();return;} String text=subtitles.getText().toString().replaceAll("(?m)^\\s*\\d+\\s*$","").replaceAll("(?m)^.*-->.*$","").trim(); if(!text.isEmpty())tts.speak(text,android.speech.tts.TextToSpeech.QUEUE_FLUSH,null,"preview");});
 Switch narration=new Switch(this);narration.setText("Thuyết minh đồng bộ video");root.addView(narration);narration.setOnCheckedChangeListener((v,on)->{narrate=on;lastSpoken="";if(!on&&tts!=null)tts.stop();});
 button(root,"Dừng đọc",()->{narration.setChecked(false);if(tts!=null)tts.stop();});
 android.widget.ScrollView scroll=new android.widget.ScrollView(this);scroll.setFillViewport(true);video.setLayoutParams(new LinearLayout.LayoutParams(-1,(int)(220*getResources().getDisplayMetrics().density)));subtitles.setLayoutParams(new LinearLayout.LayoutParams(-1,(int)(180*getResources().getDisplayMetrics().density)));scroll.addView(root);setContentView(scroll); handler.post(tick);
 tts=new android.speech.tts.TextToSpeech(this,status->{if(status==android.speech.tts.TextToSpeech.SUCCESS && tts!=null){voiceReady=tts.setLanguage(new java.util.Locale("vi","VN"))>=0;}});
 subtitles.setText(getPreferences(MODE_PRIVATE).getString("draft","")); String saved=getPreferences(MODE_PRIVATE).getString("video",""); if(!saved.isEmpty()){selectedVideo=android.net.Uri.parse(saved);video.setVideoURI(selectedVideo);}
 }
 final Runnable tick=new Runnable(){public void run(){int position=video.getCurrentPosition();String current=Srt.at(cues,position);caption.setText(current);
 if(lastPosition>=0&&Math.abs(position-lastPosition)>1000){lastSpoken="";if(narrate&&tts!=null)tts.stop();}
 if(narrate&&voiceReady&&video.isPlaying()){if(!current.equals(lastSpoken)){lastSpoken=current;if(!current.isEmpty())tts.speak(current,android.speech.tts.TextToSpeech.QUEUE_FLUSH,null,"cue-"+position);}}else if(narrate&&tts!=null){tts.stop();lastSpoken="";}
 lastPosition=position;handler.postDelayed(this,150);}};
 @Override protected void onDestroy(){handler.removeCallbacks(tick);if(activeMix!=null)activeMix.cancel();if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
 void timeline(){
 final java.util.List<Srt.Cue> list;try{list=Srt.parse(subtitles.getText().toString());}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();return;}
 if(list.isEmpty()){Toast.makeText(this,"Chưa có phụ đề",Toast.LENGTH_SHORT).show();return;}
 String[] rows=new String[list.size()];for(int i=0;i<rows.length;i++){Srt.Cue c=list.get(i);rows[i]=Srt.format(c.start)+" → "+Srt.format(c.end)+"\n"+c.text;}
 new android.app.AlertDialog.Builder(this).setTitle("Timeline • "+rows.length+" câu").setItems(rows,(d,i)->editCue(list,i)).setNegativeButton("Đóng",null).show();
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
 new android.app.AlertDialog.Builder(this).setTitle("Âm lượng video").setView(fields).setNegativeButton("Hủy",null).setPositiveButton("Xuất",(d,w)->{try{activeMix=new MixExport(this,source,audio,target,original.getProgress()/100f,voice.getProgress()/100f,new int[]{0,720,1080,2160}[resolution.getSelectedItemPosition()],burn.isChecked()?Srt.parse(subtitles.getText().toString()):java.util.Collections.emptyList(),16+captionSize.getProgress(),new int[]{0xffffffff,0xffffff00,0xff80ffff}[captionColor.getSelectedItemPosition()],captionPosition.getSelectedItemPosition(),captionBackground.isChecked(),duck.isChecked()?Srt.parse(subtitles.getText().toString()):java.util.Collections.emptyList(),()->activeMix=null);activeMix.start();}catch(Exception e){activeMix=null;Toast.makeText(this,"Không thể trộn: "+e.getMessage(),Toast.LENGTH_LONG).show();}}).show();
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
 String[] voices={"alloy","nova","onyx","shimmer","coral"};Spinner spinner=new Spinner(this);spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,voices));
 LinearLayout fields=new LinearLayout(this);fields.setOrientation(LinearLayout.VERTICAL);fields.addView(key);fields.addView(spinner);
 new android.app.AlertDialog.Builder(this).setTitle("Giọng AI • gửi phụ đề đến OpenAI (có thể tính phí)").setView(fields).setNegativeButton("Hủy",null).setPositiveButton("Xuất WAV",(d,w)->{try{voiceCues=Srt.parse(subtitles.getText().toString());if(voiceCues.isEmpty())throw new IOException("Chưa có phụ đề");voiceKey=key.getText().toString().trim();key.setText("");if(voiceKey.isEmpty())return;voiceName=voices[spinner.getSelectedItemPosition()];Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("audio/wav");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"dub.wav");startActivityForResult(i,6);}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}}).show();
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
 final EditText key=new EditText(this);key.setHint(gemini?"API key Gemini":"API key OpenAI");key.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
 final EditText model=new EditText(this);model.setSingleLine(true);model.setText(gemini?"gemini-3.5-flash-lite":"gpt-5-mini");LinearLayout fields=new LinearLayout(this);fields.setOrientation(LinearLayout.VERTICAL);fields.addView(key);fields.addView(model);
 new android.app.AlertDialog.Builder(this).setTitle("Dịch AI • gửi phụ đề tới "+(gemini?"Gemini":"OpenAI")+" (có thể tính phí)").setView(fields).setNegativeButton("Hủy",null).setPositiveButton("Dịch",(d,w)->{
 String selectedModel=model.getText().toString().trim();if(!selectedModel.matches("[A-Za-z0-9._-]{1,100}")){Toast.makeText(this,"Tên model không hợp lệ",Toast.LENGTH_SHORT).show();return;} String token=key.getText().toString().trim();key.setText("");if(token.isEmpty())return;
 String snapshot=subtitles.getText().toString();final java.util.List<Srt.Cue> original;try{original=Srt.parse(snapshot);}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();return;}
 if(original.isEmpty())return;
 Toast.makeText(this,"Đang dịch AI...",Toast.LENGTH_LONG).show();
 new Thread(()->{try{String result=AiTranslator.translate(token,original,gemini,selectedModel);runOnUiThread(()->{if(isDestroyed())return;if(snapshot.equals(subtitles.getText().toString())){subtitles.setText(result);cues=Srt.parse(result);}else Toast.makeText(this,"Phụ đề đã thay đổi; không ghi đè",Toast.LENGTH_LONG).show();});}catch(Exception e){runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Dịch AI thất bại: "+e.getMessage(),Toast.LENGTH_LONG).show();});}}).start();
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
 void button(LinearLayout root,String label,Runnable action){Button b=new Button(this);b.setText(label);b.setOnClickListener(v->action.run());root.addView(b);}
 void pick(String mime,int code){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType(mime);i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,code);}
 @Override protected void onPause(){super.onPause();getPreferences(MODE_PRIVATE).edit().putString("draft",subtitles.getText().toString()).apply();if(video.isPlaying())video.pause();if(tts!=null)tts.stop();}
 @Override protected void onActivityResult(int code,int result,Intent data){super.onActivityResult(code,result,data);if(code==6&&(result!=RESULT_OK||data==null||data.getData()==null)){voiceKey=null;voiceCues=null;}if(result!=RESULT_OK||data==null||data.getData()==null)return;
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
 else if(code==3){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){out.write(subtitles.getText().toString().getBytes(StandardCharsets.UTF_8));}Toast.makeText(this,"Đã lưu phụ đề",Toast.LENGTH_SHORT).show();}
 }catch(Exception e){Toast.makeText(this,"Không thể xử lý tệp: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
}
