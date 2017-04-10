/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.analysis.kr.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.lucene.analysis.kr.morph.CompoundEntry;
import org.apache.lucene.analysis.kr.morph.MorphException;
import org.apache.lucene.analysis.kr.morph.WordEntry;

public class DictionaryUtil {

	private static Supplier<Trie<String, WordEntry>> DICTIONARY = Memoizer.memoize(() -> {
				try {
					return loadDictionary();
				} catch (MorphException e) {
					throw new RuntimeException("Failed to initialize DICTIONARY.", e);
				}
			}
	);
	
	private static Supplier<Map<String, String>> JOSAS = Memoizer.memoize(() -> readDict(KoreanEnv.FILE_JOSA));
	
	private static Supplier<Map<String, String>> EOMIS = Memoizer.memoize(() -> readDict(KoreanEnv.FILE_EOMI));

    private static Supplier<Map<String, String>> PREFIXES = Memoizer.memoize(() -> readDict(KoreanEnv.FILE_PREFIX));

    private static Supplier<Map<String, String>> SUFFIXES = Memoizer.memoize(() -> readDict(KoreanEnv.FILE_SUFFIX));

	private static Supplier<Map<String, WordEntry>> UNCOMPOUNDS = Memoizer.memoize(() -> {
		Map<String, WordEntry> uncompounds = new HashMap<>();
		List<String> lines;

		try {
			lines = FileUtil.readLines(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_UNCOMPOUNDS), "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize UNCOMPOUNDS dictionary.", e);
		}

		for (String compound : lines) {
			String[] infos = StringUtil.split(compound, ":");
			if (infos.length != 2) continue;
			WordEntry entry = new WordEntry(infos[0].trim(), "90000X".toCharArray());
			entry.setCompounds(compoundArrayToList(infos[1], StringUtil.split(infos[1], ",")));
			uncompounds.put(entry.getWord(), entry);
		}

		return Collections.unmodifiableMap(uncompounds);
	});

	private static Supplier<Map<String, String>> CJWORDS = Memoizer.memoize(() -> {
		Map<String, String> cjwords = new HashMap<>();
		List<String> lines;

		try {
			lines = FileUtil.readLines(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_CJ), "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize CJWORDS dictionary.", e);
		}

		for (String cj : lines) {
			String[] infos = StringUtil.split(cj, ":");
			if (infos.length != 2) continue;
			cjwords.put(infos[0], infos[1]);
		}

		return Collections.unmodifiableMap(cjwords);
	});

	/**
	 * 사전을 로드한다.
	 */
	public synchronized static Trie<String, WordEntry> loadDictionary() throws MorphException {

        Trie<String, WordEntry> dictionary = new Trie<String, WordEntry>(true);
		List<String> strList = null;
		List<String> compounds = null;
		try {
			strList = FileUtil.readLines(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_DICTIONARY),"UTF-8");
			strList.addAll(FileUtil.readLines(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_EXTENSION),"UTF-8"));
			compounds = FileUtil.readLines(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_COMPOUNDS),"UTF-8");			
		} catch (IOException e) {			
			new MorphException(e.getMessage(),e);
		} catch (Exception e) {
			new MorphException(e.getMessage(),e);
		}
		if(strList==null) throw new MorphException("dictionary is null");;
		
		for(String str:strList) {
			String[] infos = StringUtil.split(str,",");
			if(infos.length!=2) continue;
			infos[1] = infos[1].trim();
			if(infos[1].length()==6) infos[1] = infos[1].substring(0,5)+"000"+infos[1].substring(5);
			
			WordEntry entry = new WordEntry(infos[0].trim(),infos[1].trim().toCharArray());
			dictionary.add(entry.getWord(), entry);
		}
		
		for(String compound: compounds) {		
			String[] infos = StringUtil.split(compound,":");
			if(infos.length!=2) continue;
			WordEntry entry = new WordEntry(infos[0].trim(),"20000000X".toCharArray());
			entry.setCompounds(compoundArrayToList(infos[1], StringUtil.split(infos[1],",")));
			dictionary.add(entry.getWord(), entry);
		}

        return dictionary;
	}
	
	public static Iterator findWithPrefix(String prefix) throws MorphException {
		return DICTIONARY.get().getPrefixedBy(prefix);
	}

	public static WordEntry getWord(String key) throws MorphException {		
		return (WordEntry) DICTIONARY.get().get(key);
	}
	
	public static WordEntry getWordExceptVerb(String key) throws MorphException {		
		WordEntry entry = getWord(key);		
		if(entry==null) return null;
		
		if(entry.getFeature(WordEntry.IDX_NOUN)=='1'||
				entry.getFeature(WordEntry.IDX_BUSA)=='1') return entry;
		return null;
	}
	
	public static WordEntry getNoun(String key) throws MorphException {	

		WordEntry entry = getWord(key);
		if(entry==null) return null;
		
		if(entry.getFeature(WordEntry.IDX_NOUN)=='1') return entry;
		return null;
	}
	
	public static WordEntry getCNoun(String key) throws MorphException {	

		WordEntry entry = getWord(key);
		if(entry==null) return null;

		if(entry.getFeature(WordEntry.IDX_NOUN)=='1' || entry.getFeature(WordEntry.IDX_NOUN)=='2') return entry;
		return null;
	}
	
	public static WordEntry getVerb(String key) throws MorphException {
		
		WordEntry entry = getWord(key);	
		if(entry==null) return null;

		if(entry.getFeature(WordEntry.IDX_VERB)=='1') {
			return entry;
		}
		return null;
	}
	
	public static WordEntry getAdverb(String key) throws MorphException {
		WordEntry entry = getWord(key);
		if(entry==null) return null;

		if(entry.getFeature(WordEntry.IDX_BUSA)=='1') return entry;
		return null;
	}
	
	public static WordEntry getBusa(String key) throws MorphException {
		WordEntry entry = getWord(key);
		if(entry==null) return null;

		if(entry.getFeature(WordEntry.IDX_BUSA)=='1'&&entry.getFeature(WordEntry.IDX_NOUN)=='0') return entry;
		return null;
	}
	
	public static WordEntry getIrrVerb(String key, char irrType) throws MorphException {
		WordEntry entry = getWord(key);
		if(entry==null) return null;

		if(entry.getFeature(WordEntry.IDX_VERB)=='1'&&
				entry.getFeature(WordEntry.IDX_REGURA)==irrType) return entry;
		return null;
	}
	
	public static WordEntry getBeVerb(String key) throws MorphException {
		WordEntry entry = getWord(key);
		if(entry==null) return null;
		
		if(entry.getFeature(WordEntry.IDX_BEV)=='1') return entry;
		return null;
	}
	
	public static WordEntry getDoVerb(String key) throws MorphException {
		WordEntry entry = getWord(key);
		if(entry==null) return null;
		
		if(entry.getFeature(WordEntry.IDX_DOV)=='1') return entry;
		return null;
	}
	
	public static WordEntry getUncompound(String key) throws MorphException {
		return UNCOMPOUNDS.get().get(key);
	}
	
	public static String getCJWord(String key) throws MorphException {
		return CJWORDS.get().get(key);
	}
	
	public static boolean existJosa(String str) throws MorphException {
		return JOSAS.get().get(str) != null;
	}
	
	public static boolean existEomi(String str)  throws MorphException {
        return EOMIS.get().get(str) != null;
	}
	
	public static boolean existPrefix(String str)  throws MorphException {
        return PREFIXES.get().get(str) != null;
	}
	
	public static boolean existSuffix(String str)  throws MorphException {
        return SUFFIXES.get().get(str) != null;
	}
	
	/**
	 * ㄴ,ㄹ,ㅁ,ㅂ과 eomi 가 결합하여 어미가 될 수 있는지 점검한다.
	 * @param s
	 * @param end
	 * @return
	 */
	public static String combineAndEomiCheck(char s, String eomi) throws MorphException {
	
		if(eomi==null) eomi="";

		if(s=='ㄴ') eomi = "은"+eomi;
		else if(s=='ㄹ') eomi = "을"+eomi;
		else if(s=='ㅁ') eomi = "음"+eomi;
		else if(s=='ㅂ') eomi = "습"+eomi;
		else eomi = s+eomi;

		if(existEomi(eomi)) return eomi;		

		return null;
		
	}
	
	private static Map<String, String> readDict(String dic) {
		
		try {
            String path = KoreanEnv.getInstance().getValue(dic);
            HashMap<String, String> map = new HashMap<String, String>();

			List<String> line = FileUtil.readLines(path,"UTF-8");
			for(int i=1;i<line.size();i++) {
				map.put(line.get(i).trim(), line.get(i));
			}

            return Collections.unmodifiableMap(map);

		} catch (Exception e) {
 		    throw new RuntimeException(String.format("Failed to initialize %s dictionary ", dic), e);
		}
	}
	
	private static List compoundArrayToList(String source, String[] arr) {
		List list = new ArrayList();
		for(String str: arr) {
			CompoundEntry ce = new CompoundEntry(str);
			ce.setOffset(source.indexOf(str));
			list.add(ce);
		}
		return list;
	}
}

