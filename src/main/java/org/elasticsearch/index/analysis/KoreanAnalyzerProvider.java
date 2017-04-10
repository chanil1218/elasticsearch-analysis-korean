package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.kr.KoreanAnalyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

import java.io.IOException;

public class KoreanAnalyzerProvider extends AbstractIndexAnalyzerProvider<KoreanAnalyzer> {

    private final KoreanAnalyzer analyzer;

    @Inject
    public KoreanAnalyzerProvider(IndexSettings indexSettings, Environment env, @Assisted String name, @Assisted Settings settings) throws IOException {
        super(indexSettings, name, settings);
        analyzer = new KoreanAnalyzer();
    }

    @Override
    public KoreanAnalyzer get() {
        return this.analyzer;
    }
}
