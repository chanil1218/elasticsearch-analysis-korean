Korean Analysis for ElasticSearch
==================================

The Korean Analysis plugin integrates Lucene Korean analysis module into elasticsearch.

In order to install the plugin, simply run: `bin/plugin -install chanil1218/elasticsearch-analysis-korean/1.3.0`

if above command is not working then try this: 
```
bin/plugin -url https://dl-web.dropbox.com/spa/grpekzky9x5y6mc/elastic-analysis-korean/public/elasticsearch-analysis-korean-1.3.0.zip -install analysis-korean
```

Or you can clone this git repository, set correct version, build and just copy the jar file to `plugins/analysis-korean/` directory.

    --------------------------------------------------
    | Korean Analysis Plugin      | ElasticSearch    |
    --------------------------------------------------
    | master                      | 0.90.1 -> master |
    --------------------------------------------------
    | 1.3.0                       | 0.90.1 -> master |
    --------------------------------------------------
    | 1.2.0                       | 0.90.0           |
    --------------------------------------------------
    | 1.1.0                       | 0.19.9           |
    --------------------------------------------------
    | 1.0.0                       | 0.19.2           |
    --------------------------------------------------

The plugin includes the `kr_analyzer` analyzer, `kr_tokenizer` tokenizer, and `kr_filter` token filter.


Lucene Korean Analysis Module
==============================

http://cafe.naver.com/korlucene
