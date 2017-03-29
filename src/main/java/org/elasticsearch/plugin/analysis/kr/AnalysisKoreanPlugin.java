package org.elasticsearch.plugin.analysis.kr;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.KoreanAnalyzerProvider;
import org.elasticsearch.index.analysis.KoreanFilterFactory;
import org.elasticsearch.index.analysis.KoreanTokenizerFactory;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.Collections;
import java.util.Map;

public class AnalysisKoreanPlugin extends Plugin implements AnalysisPlugin {

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        return Collections.singletonMap("kr_filter", KoreanFilterFactory::new);
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        return Collections.singletonMap("kr_tokenizer", KoreanTokenizerFactory::new);
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        return Collections.singletonMap("kr_analyzer", KoreanAnalyzerProvider::new);
    }
}
