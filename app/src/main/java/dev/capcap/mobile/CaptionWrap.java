package dev.capcap.mobile;
final class CaptionWrap {
 interface Measure {float width(String text);}
 static String wrap(String text,float max,Measure measure){
 StringBuilder result=new StringBuilder();
 String[] lines=text.split("\\n",-1);
 for(int i=0;i<lines.length;i++){
 if(i>0)result.append('\n');
 String line="";
 for(String word:lines[i].trim().split("\\s+")){
 if(word.isEmpty())continue;
 String candidate=line.isEmpty()?word:line+" "+word;
 if(measure.width(candidate)<=max){line=candidate;continue;}
 if(!line.isEmpty()){result.append(line).append('\n');line="";}
 for(int offset=0;offset<word.length();){int cp=word.codePointAt(offset);String next=new String(Character.toChars(cp));if(!line.isEmpty()&&measure.width(line+next)>max){result.append(line).append('\n');line="";}line+=next;offset+=Character.charCount(cp);}
 }
 result.append(line);
 }
 return result.toString();
 }
}
