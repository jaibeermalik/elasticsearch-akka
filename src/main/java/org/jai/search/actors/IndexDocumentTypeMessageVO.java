package org.jai.search.actors;

import org.jai.search.config.ElasticSearchIndexConfig;
import org.jai.search.config.IndexDocumentType;

public class IndexDocumentTypeMessageVO
{
    private ElasticSearchIndexConfig config;

    private IndexDocumentType indexDocumentType;

    public IndexDocumentTypeMessageVO config(final ElasticSearchIndexConfig elasticSearchIndexConfig)
    {
        config = elasticSearchIndexConfig;
        return this;
    }

    public IndexDocumentTypeMessageVO documentType(final IndexDocumentType indexDocumentType)
    {
        this.indexDocumentType = indexDocumentType;
        return this;
    }

    public ElasticSearchIndexConfig getConfig()
    {
        return config;
    }

    public IndexDocumentType getIndexDocumentType()
    {
        return indexDocumentType;
    }
}
