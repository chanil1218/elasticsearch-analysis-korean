package org.elasticsearch.plugin.analysis.kr;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.KoreanAnalysisBinderProcessor;
import org.elasticsearch.plugins.AbstractPlugin;

public class AnalysisKoreanPlugin extends AbstractPlugin {

    @Override
    public String name() {
            return "analysis-korean";
        }   

    @Override
    public String description() {
            return "Korean analysis support";
        }   

    public void onModule(AnalysisModule module) {
            module.addProcessor(new KoreanAnalysisBinderProcessor());
        }   
}
