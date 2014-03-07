package org.jai.search.index.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Field.Index;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.index.IndexProductDataService;
import org.jai.search.model.ElasticSearchIndexConfig;
import org.jai.search.model.IndexConfig;
import org.jai.search.model.Product;
import org.jai.search.model.ProductGroup;
import org.jai.search.setup.SetupIndexService;
import org.jai.search.setup.impl.SetupIndexWorkerActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinPool;

public class IndexProductDataWorkerActor extends UntypedActor
{
	private static final Logger logger = LoggerFactory.getLogger(IndexProductDataWorkerActor.class);
	
	private IndexProductDataService indexProductDataService;
	
	public IndexProductDataWorkerActor(IndexProductDataService indexProductDataService)
	{
		this.indexProductDataService = indexProductDataService;
	}
	
    public void onReceive(Object message)
    {
    	logger.debug("Worker Actor message is:" + message);
    	if (message instanceof IndexConfig)
        {
    		IndexConfig indexConfig = (IndexConfig) message;
			indexProductDataService.indexProduct(indexConfig.getConfig(), indexConfig.getProduct());
			indexConfig.indexDone(true);
			getSender().tell(indexConfig, getSelf());
       }
//    	 if (message instanceof Map)
//         {
//         	Map<ElasticSearchIndexConfig, Product> data = (Map<ElasticSearchIndexConfig, Product>) message;
////         	Long productGroupId = (Long) message;
//         	for (ElasticSearchIndexConfig config : data.keySet()) 
//         	{
//         		List<Product> list = new ArrayList<Product>();
//         		list.add(data.get(config));
//				indexProductDataService.indexAllProducts(config, list); 
//         	}
////        	IndexResponse result = indexDocument(config);
//        
////        	getSender().tell(result, getSelf());
//        }
        else
        {
            unhandled(message);
        }
    }
    
    private IndexResponse indexDocument(IndexRequestBuilder indexRequestBuilder)
    {
        return indexRequestBuilder.execute().actionGet();
    }
}
