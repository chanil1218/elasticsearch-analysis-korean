package org.elasticsearch.index.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.kr.KoreanTokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettings;

public class KoreanTokenizerFactory extends AbstractTokenizerFactory {
  @Inject
  public KoreanTokenizerFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings) {
    super(index, indexSettings, name, settings);
  }
  @Override
  public Tokenizer create(Reader input) {
    return new KoreanTokenizer(version, input);
  }   
}
