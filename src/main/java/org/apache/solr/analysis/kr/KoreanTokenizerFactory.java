package org.apache.solr.analysis.kr;

import java.io.Reader;


import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.kr.KoreanTokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.Version;

public class KoreanTokenizerFactory extends TokenizerFactory {

	private Version version;
	
	public KoreanTokenizerFactory() {
		version = Version.LUCENE_42;
	}
	
	public KoreanTokenizerFactory(Version v) {
		version = v;
	}
	
	public Tokenizer create(Reader input) {
		return new KoreanTokenizer(version, input);
	}

}
