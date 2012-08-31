package org.apache.lucene.analysis.kr.utils;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.kr.morph.MorphException;

public class HanjaUtils {

	private static Map<String, char[]> mapHanja;
	
	public synchronized static void loadDictionary() throws MorphException {
		try {
			List<String> strList = FileUtil.readLines("org/apache/lucene/analysis/kr/dic/mapHanja.dic","UTF-8");
			mapHanja = new HashMap();		
		
			for(int i=0;i<strList.size();i++) {
				
				if(strList.get(i).length()<1||
						strList.get(i).indexOf(",")==-1) continue;

				String[] hanInfos = StringUtil.split(strList.get(i),",");

				if(hanInfos.length!=2) continue;
				
				String hanja = StringEscapeUtil.unescapeJava(hanInfos[0]);

				mapHanja.put(hanja, hanInfos[1].toCharArray());
			}			
		} catch (IOException e) {
			throw new MorphException(e);
		}
	}
	
    public static char[] convertToHangul(char hanja) throws MorphException {
 
    	if(mapHanja==null)  loadDictionary();

		if(hanja>0x9FFF||hanja<0x3400) return new char[]{hanja};
		
    	return mapHanja.get(new String(new char[]{hanja}));
    }
}
