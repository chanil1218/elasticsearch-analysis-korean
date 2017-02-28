package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.kr.KoreanTokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettingsService;

public class KoreanTokenizerFactory extends AbstractTokenizerFactory {

  @Inject
  public KoreanTokenizerFactory(Index index, IndexSettingsService indexSettings, String name, Settings settings) {
    super(index, indexSettings.getSettings(), name, settings);
  }

  @Override
  public Tokenizer create() {
    return new KoreanTokenizer();
  }
}
