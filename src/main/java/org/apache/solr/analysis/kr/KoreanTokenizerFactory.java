package org.apache.solr.analysis.kr;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.kr.KoreanTokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.Version;

public class KoreanTokenizerFactory extends TokenizerFactory {

	private Version version;

	public KoreanTokenizerFactory() {
		super(new HashMap<String, String>());
		version = Version.LUCENE_4_9;
	}

	public KoreanTokenizerFactory(Version v) {
		super(new HashMap<String, String>());
		version = v;
	}

	protected KoreanTokenizerFactory(Map<String, String> args) {
		super(args);
	}

	@Override
	public Tokenizer create(AttributeFactory attributeFactory, Reader reader) {
		return new KoreanTokenizer(version, attributeFactory, reader);
	}
}
