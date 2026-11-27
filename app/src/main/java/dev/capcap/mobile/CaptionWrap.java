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
 for(int offset=0;offset<word.length();){int end=offset+Character.charCount(word.codePointAt(offset));while(end<word.length()){int cp=word.codePointAt(end);int type=Character.getType(cp);if(type!=Character.NON_SPACING_MARK&&type!=Character.COMBINING_SPACING_MARK&&type!=Character.ENCLOSING_MARK)break;end+=Character.charCount(cp);}String next=word.substring(offset,end);if(!line.isEmpty()&&measure.width(line+next)>max){result.append(line).append('\n');line="";}line+=next;offset=end;}
 }
 result.append(line);
 }
 return result.toString();
 }
}
