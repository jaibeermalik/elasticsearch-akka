package org.jai.search.model;


public class IndexConfig 
{
	private ElasticSearchIndexConfig config;
	
	private Long productId;

	private Product product;
	
	private boolean indexDone;
	
	public IndexConfig config(ElasticSearchIndexConfig elasticSearchIndexConfig)
	{
		config = elasticSearchIndexConfig;
		return this;
	}
	
	public IndexConfig product(Product product)
	{
		this.product = product;
		return this;
	}
	
	public IndexConfig productId(Long productId)
	{
		this.productId = productId;
		return this;
	}
	
	public IndexConfig indexDone(boolean indexDone)
	{
		this.indexDone = indexDone;
		return this;
	}
	
	public boolean isIndexDone()
	{
		return indexDone;
	}
	
	public ElasticSearchIndexConfig getConfig() {
		return config;
	}

	public Product getProduct() {
		return product;
	}

	public Long getProductId() {
		return productId;
	}	
}
