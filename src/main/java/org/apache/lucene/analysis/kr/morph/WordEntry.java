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
package org.apache.lucene.analysis.kr.morph;


import java.util.ArrayList;
import java.util.List;

public class WordEntry {

	public static final int IDX_NOUN = 0;
	public static final int IDX_VERB = 1;
	public static final int IDX_BUSA = 2;
	public static final int IDX_DOV = 3;
	public static final int IDX_BEV = 4;
	public static final int IDX_NE = 5;
	public static final int IDX_ADJ = 6; // 형용사
	public static final int IDX_NPR = 7;  // 명사의 분류 (M:Measure)
	public static final int IDX_CNOUNX = 8; 
	public static final int IDX_REGURA = 9;
	
	/**
	 * 단어
	 */
	private String word;
	
	/**
	 * 단어특성
	 */
	private char[] features;
	
	private List<CompoundEntry> compounds = new ArrayList();
	
	public WordEntry() {
		
	}
	
	public WordEntry(String word) {
		this.word = word;
	}
	
	public WordEntry(String word, char[] cs) {
		this.word = word;
		this.features = cs;
	}
	
	public WordEntry(String word, List c) {
		this.word = word;
		this.compounds = c;
	}
	
	public void setWord(String w) {
		this.word = w;
	}
	
	public String getWord() {
		return this.word;
	}
	
	public void setFeatures(char[] cs) {
		this.features = cs;
	}
	
	public char getFeature(int index) {
		if(features==null||features.length<index) return '0';		
		return features[index];
	}
	
	public char[] getFeatures() {
		return this.features;
	}
	
	public void setCompounds(List<CompoundEntry> c) {
		this.compounds = c;
	}
	
	public List<CompoundEntry> getCompounds() {
		return this.compounds;
	}
	
}
