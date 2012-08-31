package org.elasticsearch.index.analysis;
 
import java.io.IOException;
import org.apache.lucene.analysis.kr.KoreanAnalyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettings;

public class KoreanAnalyzerProvider extends AbstractIndexAnalyzerProvider<KoreanAnalyzer> {

    private final KoreanAnalyzer analyzer;

    @Inject
    public KoreanAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env, @Assisted String name, @Assisted Settings settings) throws IOException {
            super(index, indexSettings, name, settings);
            analyzer = new KoreanAnalyzer(version);
        }       

    @Override
    public KoreanAnalyzer get() {
            return this.analyzer;
        }       
}
