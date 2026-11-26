package dev.capcap.mobile;
import java.util.*;
import java.util.regex.*;
final class Srt {
 static final Pattern TIME=Pattern.compile("(\\d{2,}):(\\d{2}):(\\d{2})[,.](\\d{3})");
 static final class Cue {final long start,end;final String text;Cue(long s,long e,String t){start=s;end=e;text=t;}}
 static long millis(String s){Matcher m=TIME.matcher(s.trim());if(!m.matches())throw new IllegalArgumentException("Mốc thời gian SRT không hợp lệ");long h=Long.parseLong(m.group(1)),min=Long.parseLong(m.group(2)),sec=Long.parseLong(m.group(3));if(min>59||sec>59||h>9999)throw new IllegalArgumentException("Mốc thời gian ngoài phạm vi");return ((h*60+min)*60+sec)*1000+Long.parseLong(m.group(4));}
 static List<Cue> parse(String input){List<Cue> out=new ArrayList<>();String normalized=input.replace("\uFEFF","").replace("\r\n","\n").replace('\r','\n').trim();if(normalized.isEmpty())return out;for(String block:normalized.split("\\n[ \\t]*\\n+")){String[] lines=block.split("\\n");int i=lines[0].trim().matches("\\d+")?1:0;if(lines.length<=i+1)throw new IllegalArgumentException("Thiếu nội dung phụ đề");String[] times=lines[i].split("-->",-1);if(times.length!=2)throw new IllegalArgumentException("Thiếu mốc thời gian");long s=millis(times[0]),e=millis(times[1]);if(e<=s)throw new IllegalArgumentException("Thời gian kết thúc phải sau bắt đầu");StringBuilder text=new StringBuilder();for(int j=i+1;j<lines.length;j++){if(j>i+1)text.append('\n');text.append(lines[j]);}out.add(new Cue(s,e,text.toString()));}out.sort(Comparator.comparingLong(c->c.start));return out;}
 static String at(List<Cue> cues,long ms){StringBuilder b=new StringBuilder();for(Cue c:cues){if(c.start>ms)break;if(ms<c.end){if(b.length()>0)b.append('\n');b.append(c.text);}}return b.toString();}
}
