package org.apache.solr.analysis.kr;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;


import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.kr.KoreanTokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;

public class KoreanTokenizerFactory extends TokenizerFactory {

    public KoreanTokenizerFactory() {
        super(new HashMap<String, String>());
    }

    protected KoreanTokenizerFactory(Map<String, String> args) {
        super(args);
    }

    @Override
    public Tokenizer create(AttributeFactory attributeFactory) {
        return new KoreanTokenizer();
    }

}