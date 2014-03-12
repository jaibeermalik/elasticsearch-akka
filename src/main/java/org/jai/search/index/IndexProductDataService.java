package org.jai.search.index;

import org.jai.search.config.ElasticSearchIndexConfig;
import org.jai.search.model.Product;

import java.util.List;

public interface IndexProductDataService
{
    void indexAllProducts(ElasticSearchIndexConfig config, List<Product> products);

    void indexProduct(ElasticSearchIndexConfig config, Product product);

    boolean isProductExists(ElasticSearchIndexConfig config, Long productId);

    void deleteProduct(ElasticSearchIndexConfig config, Long productId);
}
