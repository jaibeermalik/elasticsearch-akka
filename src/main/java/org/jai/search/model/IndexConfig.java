package org.jai.search.model;

import org.jai.search.config.ElasticSearchIndexConfig;

public class IndexConfig
{
    private ElasticSearchIndexConfig config;

    private Long productId;

    private Product product;

    private boolean indexDone;

    public IndexConfig config(final ElasticSearchIndexConfig elasticSearchIndexConfig)
    {
        config = elasticSearchIndexConfig;
        return this;
    }

    public IndexConfig product(final Product product)
    {
        this.product = product;
        return this;
    }

    public IndexConfig productId(final Long productId)
    {
        this.productId = productId;
        return this;
    }

    public IndexConfig indexDone(final boolean indexDone)
    {
        this.indexDone = indexDone;
        return this;
    }

    public boolean isIndexDone()
    {
        return indexDone;
    }

    public ElasticSearchIndexConfig getConfig()
    {
        return config;
    }

    public Product getProduct()
    {
        return product;
    }

    public Long getProductId()
    {
        return productId;
    }
}
