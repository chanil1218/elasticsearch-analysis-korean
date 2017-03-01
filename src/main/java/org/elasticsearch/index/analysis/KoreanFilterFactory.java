package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.kr.KoreanFilter;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettingsService;

public class KoreanFilterFactory extends AbstractTokenFilterFactory { 

  @Inject    
  public KoreanFilterFactory(Index index, IndexSettingsService indexSettings, String name, Settings settings) {
    super(index, indexSettings.indexSettings(), name, settings);
  }

  private boolean bigrammable = true;
  private boolean hasOrigin = true;

  public TokenStream create(TokenStream tokenstream) { 
    return new KoreanFilter(tokenstream, bigrammable, hasOrigin); 
  }   

  public void setBigrammable(boolean bool) { 
    this.bigrammable = bool; 
  }   

  public void setHasOrigin(boolean bool) { 
    this.hasOrigin = bool; 
  }   

} 
