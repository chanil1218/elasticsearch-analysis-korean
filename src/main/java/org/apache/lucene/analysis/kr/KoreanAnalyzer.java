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
 * @version $Id: KoreanAnalyzer.java,v 1.1 2012/02/08 15:00:11 smlee0818 Exp $
 */
public class KoreanAnalyzer extends StopwordAnalyzerBase {
	
	  /** Default maximum allowed token length */
	  public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	  private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	  /**
	   * Specifies whether deprecated acronyms should be replaced with HOST type.
	   * See {@linkplain "https://issues.apache.org/jira/browse/LUCENE-1068"}
	   */
	  private final boolean replaceInvalidAcronym;
	  
	  private Set stopSet;
	  
	  private boolean bigrammable = true;
	  
	  private boolean hasOrigin = true;
	  
	  private boolean exactMatch = false;
  
	  public static final String DIC_ENCODING = "UTF-8";

	  /** An unmodifiable set containing some common English words that are usually not
	  useful for searching. */
	  public static final Set<?> STOP_WORDS_SET; 
	  

	 static
	 {
		List stopWords = Arrays.asList(new String[] { "a", "an", "and", "are", "as", "at", "be", "but", "by", 
				"for", "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", 
				"their", "then", "there", "these", "they", "this", "to", "was", "will", "with",
				"이","그","저","것","수","등","들","및","에서","그리고","그래서","또","또는"}
		);
		
	    CharArraySet stopSet = new CharArraySet(Version.LUCENE_CURRENT, stopWords.size(), false);
	 
	    stopSet.addAll(stopWords);
	    STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
	}
	  
	public KoreanAnalyzer() {
	    this(Version.LUCENE_CURRENT, STOP_WORDS_SET);
	}

	/**
	 * 검색을 위한 형태소분석
	 * @param search
	 */
	public KoreanAnalyzer(boolean exactMatch) {
	    this(Version.LUCENE_CURRENT, STOP_WORDS_SET);	    
	    this.exactMatch = exactMatch;
	}
	
	public KoreanAnalyzer(Version matchVersion, String[] stopWords) throws IOException {
	    this(matchVersion, StopFilter.makeStopSet(matchVersion, stopWords));    
	}

  /** Builds an analyzer with the stop words from the given file.
   * @see WordlistLoader#getWordSet(File)
   */
	public KoreanAnalyzer(Version matchVersion) throws IOException {     
        this(matchVersion, STOP_WORDS_SET);        
	}
		
  /** Builds an analyzer with the stop words from the given file.
   * @see WordlistLoader#getWordSet(File)
   */
	public KoreanAnalyzer(Version matchVersion, File stopwords) throws IOException {     
        this(matchVersion, WordlistLoader.getWordSet(new InputStreamReader(new FileInputStream(stopwords), DIC_ENCODING), matchVersion));
	}

  /** Builds an analyzer with the stop words from the given file.
   * @see WordlistLoader#getWordSet(File)
   */
	public KoreanAnalyzer(Version matchVersion, File stopwords, String encoding) throws IOException {
        this(matchVersion, WordlistLoader.getWordSet(new InputStreamReader(new FileInputStream(stopwords), encoding), matchVersion));
	}
		
	/** Builds an analyzer with the stop words from the given reader.
	 * @see WordlistLoader#getWordSet(Reader)
	*/
	public KoreanAnalyzer(Version matchVersion, Reader stopwords) throws IOException {
	   this(matchVersion, WordlistLoader.getWordSet(stopwords, matchVersion));
	}

	/** Builds an analyzer with the stop words from the given reader.
	 * @see WordlistLoader#getWordSet(Reader)
	*/
	public KoreanAnalyzer(Version matchVersion, Set<?> stopWords) {
	    super(matchVersion, stopWords);
	    replaceInvalidAcronym = matchVersion.onOrAfter(Version.LUCENE_24);	   
	}
	
	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
	    final KoreanTokenizer src = new KoreanTokenizer(matchVersion, reader);
	    src.setMaxTokenLength(maxTokenLength);
	    src.setReplaceInvalidAcronym(replaceInvalidAcronym);
	    TokenStream tok = new KoreanFilter(src, bigrammable, hasOrigin, exactMatch);
	    tok = new LowerCaseFilter(matchVersion, tok);
	    tok = new StopFilter(matchVersion, tok, stopwords);
	    return new TokenStreamComponents(src, tok) {
	      @Override
	      protected boolean reset(final Reader reader) throws IOException {
	        src.setMaxTokenLength(KoreanAnalyzer.this.maxTokenLength);
	        return super.reset(reader);
	      }
	    };	
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


}
