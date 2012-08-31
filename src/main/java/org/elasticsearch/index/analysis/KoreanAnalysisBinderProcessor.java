package org.elasticsearch.index.analysis;

/**
 *  */
public class KoreanAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processAnalyzers(AnalyzersBindings analyzersBindings) {
            analyzersBindings.processAnalyzer("kr_analyzer", KoreanAnalyzerProvider.class);
        }   

    @Override
    public void processTokenizers(TokenizersBindings tokenizersBindings) {
            tokenizersBindings.processTokenizer("kr_tokenizer", KoreanTokenizerFactory.class);
        }   

    @Override
    public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {
            tokenFiltersBindings.processTokenFilter("kr_filter", KoreanFilterFactory.class);
        }   
}
