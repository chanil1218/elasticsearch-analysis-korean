package org.elasticsearch.index.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.kr.KoreanTokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexDynamicSettings;

public class KoreanTokenizerFactory extends AbstractTokenizerFactory {
  @Inject
  public KoreanTokenizerFactory(Index index, @IndexDynamicSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings) {
    super(index, indexSettings, name, settings);
  }

  @Override
  public Tokenizer create() {
    return new KoreanTokenizer();
  }
}
