package org.jai.search.data;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.index.IndexProductDataService;
import org.jai.search.index.impl.IndexProductDataWorkerActor;
import org.jai.search.model.ElasticSearchIndexConfig;
import org.jai.search.model.IndexConfig;
import org.jai.search.model.Product;
import org.jai.search.model.ProductGroup;
import org.jai.search.setup.SetupIndexService;
import org.jai.search.setup.impl.SetupIndexWorkerActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinPool;

public class SampleDataGeneratorWorkerActor extends UntypedActor
{
	private static final Logger logger = LoggerFactory.getLogger(SampleDataGeneratorWorkerActor.class);
	
	private SampleDataGeneratorService sampleDataGenerator;
	private  ActorRef indexProductDataWorkerRouter;
	
	public SampleDataGeneratorWorkerActor(int nrOfIndexDataWorkers, SampleDataGeneratorService sampleDataGenerator, IndexProductDataService indexProductDataService)
	{
		this.sampleDataGenerator = sampleDataGenerator;
//		indexProductDataWorkerRouter = getContext().actorOf(Props.create(IndexProductDataWorkerActor.class, indexProductDataService).withRouter(new RoundRobinPool(nrOfIndexDataWorkers)), "indexProductDataWorkerRouter");
	}
	
    public void onReceive(Object message)
    {
    	logger.debug("Worker Actor message for SampleDataGeneratorWorkerActor is:" + message);
    	if (message instanceof IndexConfig)
        {
        	IndexConfig indexConfig = (IndexConfig) message;
//        	Long productGroupId = (Long) message;
        	Product product = sampleDataGenerator.generateProductSampleDataFor(indexConfig.getProductId());
        	indexConfig.product(product);
        	getSender().tell(indexConfig, getSelf());
        }
//        if (message instanceof Map)
//        {
//        	Map<ElasticSearchIndexConfig, Long> data = (Map<ElasticSearchIndexConfig, Long>) message;
////        	Long productGroupId = (Long) message;
//        	for (ElasticSearchIndexConfig config : data.keySet()) 
//        	{
//        		Product product = sampleDataGenerator.generateProductSampleDataFor(data.get(config));
//        		if(product !=null)
//        		{
//        			Map<ElasticSearchIndexConfig, Product> sampleData = new HashMap<ElasticSearchIndexConfig, Product>();
//        			sampleData.put(config, product);
//        			indexProductDataWorkerRouter.tell(sampleData, getSelf());
//        		}
//			}
//        	
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
