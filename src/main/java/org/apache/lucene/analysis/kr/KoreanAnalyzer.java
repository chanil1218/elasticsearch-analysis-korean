package org.apache.lucene.analysis.kr;


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

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.analysis.util.WordlistLoader;

import org.apache.lucene.util.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter}, {@link
 * LowerCaseFilter} and {@link StopFilter}, using a list of English stop words.
 *
 * @version $Id: KoreanAnalyzer.java,v 1.2 2013/04/07 13:09:33 smlee0818 Exp $
 */
public class KoreanAnalyzer extends StopwordAnalyzerBase {
	
	  /** Default maximum allowed token length */
	  public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	  private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	  private Set stopSet;
	  
	  private boolean bigrammable = true;
	  
	  private boolean hasOrigin = true;
	  
	  private boolean exactMatch = false;
	  
	  private boolean originCNoun = true;
  
	  public static final String DIC_ENCODING = "UTF-8";

	  /** An unmodifiable set containing some common English words that are usually not
	  useful for searching. */
	  public static final CharArraySet STOP_WORDS_SET; 
	  

	 static
	 {
		List stopWords = Arrays.asList(new String[] { "a", "an", "and", "are", "as", "at", "be", "but", "by", 
				"for", "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", 
				"their", "then", "there", "these", "they", "this", "to", "was", "will", "with",
				"이","그","저","것","수","등","들","및","에서","그리고","그래서","또","또는"}
		);
		
	    CharArraySet stopSet = new CharArraySet(stopWords.size(), false);
	 
	    stopSet.addAll(stopWords);
	    STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
	}
	  
	public KoreanAnalyzer() {
	    this(STOP_WORDS_SET);
	}

	@Override
	protected TokenStreamComponents createComponents(String s) {
		final KoreanTokenizer src = new KoreanTokenizer();
		src.setMaxTokenLength(maxTokenLength);
		TokenStream tok = new KoreanFilter(src, bigrammable, hasOrigin, exactMatch, originCNoun);
		tok = new LowerCaseFilter(tok);
		tok = new StopFilter(tok, stopwords);
		return new TokenStreamComponents(src, tok) {
			@Override
			protected void setReader(final Reader reader) {
				src.setMaxTokenLength(KoreanAnalyzer.this.maxTokenLength);
				super.setReader(reader);
			}
		};
	}

	/**
	 * 검색을 위한 형태소분석
	 * @param search
	 */
	public KoreanAnalyzer(boolean exactMatch) {
	    this(STOP_WORDS_SET);
	    this.exactMatch = exactMatch;
	}
	
	public KoreanAnalyzer(String[] stopWords) throws IOException {
	    this(StopFilter.makeStopSet(stopWords));
	}

	/**
	 * Builds an analyzer with the stop words from the given file.
	 *
	 * @see WordlistLoader#getWordSet(File)
	 */
	public KoreanAnalyzer(File stopwords) throws IOException {
		this(loadStopwordSet(stopwords.toPath()));
	}
		
	/** Builds an analyzer with the stop words from the given reader.
	 * @see WordlistLoader#getWordSet(Reader)
	*/
	public KoreanAnalyzer(Reader stopwords) throws IOException {
		this(loadStopwordSet(stopwords));
	}

	/** Builds an analyzer with the stop words from the given reader.
	 * @see WordlistLoader#getWordSet(Reader)
	*/
	public KoreanAnalyzer(CharArraySet stopWords) {
		super(stopWords);
	}

	/**
	 * determine whether the bigram index term is returned or not if a input word is failed to analysis
	 * If true is set, the bigram index term is returned. If false is set, the bigram index term is not returned.
	 * @param is
	 */
	public void setBigrammable(boolean is) {
		bigrammable = is;
	}
	
	/**
	 * determin whether the original term is returned or not if a input word is analyzed morphically.
	 * @param has
	 */
	public void setHasOrigin(boolean has) {
		hasOrigin = has;
	}

	/**
	 * determin whether the original compound noun is returned or not if a input word is analyzed morphically.
	 * @param has
	 */
	public void setOriginCNoun(boolean cnoun) {
		originCNoun = cnoun;
	}
	
	/**
	 * determin whether the original compound noun is returned or not if a input word is analyzed morphically.
	 * @param has
	 */
	public void setExactMatch(boolean exact) {
		exactMatch = exact;
	}
	
}
