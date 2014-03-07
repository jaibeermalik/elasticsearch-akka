package org.jai.search.setup.impl;

import java.util.HashMap;
import java.util.Map;

import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.data.SampleDataGeneratorWorkerActor;
import org.jai.search.index.IndexProductDataService;
import org.jai.search.index.impl.IndexProductDataWorkerActor;
import org.jai.search.model.ElasticSearchIndexConfig;
import org.jai.search.model.IndexConfig;
import org.jai.search.setup.SetupIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinPool;

public class SetupIndexWorkerActor extends UntypedActor
{
	private static final Logger logger = LoggerFactory.getLogger(SetupIndexWorkerActor.class);
	
	private SetupIndexService setupIndexService;
	private final ActorRef sampleDataGeneratorWorkerRouter;
	private final ActorRef indexProductDataWorkerRouter;
	private int totalProductsToIndex =0;
	private int totalProductsToIndexDone =0;
	private boolean allIndexingDone =false;
	
	public SetupIndexWorkerActor(int nrOfDataGeneratorWorkers, int nrOfIndexDataWorkers, 
			SetupIndexService setupIndexService, SampleDataGeneratorService sampleDataGeneratorService, IndexProductDataService indexProductDataService)
	{
		this.setupIndexService = setupIndexService;
		sampleDataGeneratorWorkerRouter = getContext().actorOf(Props.create(SampleDataGeneratorWorkerActor.class, nrOfIndexDataWorkers, 
				sampleDataGeneratorService, indexProductDataService ).withRouter(new RoundRobinPool(nrOfDataGeneratorWorkers)), "sampleDataGeneratorWorkerRouter");
		indexProductDataWorkerRouter = getContext().actorOf(Props.create(IndexProductDataWorkerActor.class, 
				indexProductDataService).withRouter(new RoundRobinPool(nrOfIndexDataWorkers)), "indexProductDataWorkerRouter");
	}
	
    public void onReceive(Object message)
    {
    	logger.debug("Worker Actor message for SetupIndexWorkerActor is:" + message);
    	
    	//message from master actor
    	if (message instanceof ElasticSearchIndexConfig)
          {
        	logger.debug("Worker Actor message for initial config is  received");
        	
          	ElasticSearchIndexConfig config = (ElasticSearchIndexConfig) message;
          	setupIndexService.reCreateIndex(config);
          	setupIndexService.updateIndexDocumentTypeMappings(config);
          	
          	for (int i=0; i<50; i++) 
          	{
          		IndexConfig indexConfig = new IndexConfig().config(config).productId(Long.valueOf(i));
          		sampleDataGeneratorWorkerRouter.tell(indexConfig, getSelf());
          		totalProductsToIndex++;
  			}
          }
    	//message from data generator and indexer
    	if (message instanceof IndexConfig)
        {
    		
    		IndexConfig indexConfig = (IndexConfig) message;
    		
    		if(!indexConfig.isIndexDone() && indexConfig.getProduct() != null)
    		{
    			logger.debug("Worker Actor message for indexing product id" + indexConfig.getProductId());
    			
    			indexProductDataWorkerRouter.tell(indexConfig, getSelf());
    		}
    		if(indexConfig.isIndexDone())
    		{
    			++totalProductsToIndexDone;
    			//handle successful message index count.
//    			logger.debug("Worker Actor message for indexing done for product id" + indexConfig.getProductId());
    			logger.debug("Total indexing stats are: totalProductsToIndex: {}, totalProductsToIndexDone: {}", new Object[]{totalProductsToIndex, totalProductsToIndexDone});
    			if(totalProductsToIndex == totalProductsToIndexDone)
    			{
    				logger.debug("Worker Actor message for indexing done for all products!");
    				allIndexingDone = true;
//    				sender().tell("DONEALL", null);
//    				logger.debug("Message DONEALL sent back to sender.");
//    				logger.debug("Worker Actor sender Ref is:" + this.getSender());
//    				context().stop(sampleDataGeneratorWorkerRouter);
//    				context().stop(indexProductDataWorkerRouter);
    			}
    		}
        }
    	else if(message instanceof String)
    	{
    		String msg = (String) message;
    		if(msg.equals("DONEALL"))
    		{
    			if(allIndexingDone)
    			{
    				logger.debug("Master Actor message received from workers that DONEALL!");
    				sender().tell("DONEALL", null);
    				allIndexingDone = false;
    			}
    			else
    			{
    				sender().tell("DONEALL_NO", null);
    			}
    		}
    	}
//        if (message instanceof ElasticSearchIndexConfig)
//        {
//        	ElasticSearchIndexConfig config = (ElasticSearchIndexConfig) message;
//        	setupIndexService.reCreateIndex(config);
//        	setupIndexService.updateIndexDocumentTypeMappings(config);
//        	
//        	for (int i=0; i<=50; i++) 
//        	{
//        		Map<ElasticSearchIndexConfig, Long> data = new HashMap<ElasticSearchIndexConfig, Long>();
//        		data.put(config, Long.valueOf(i));
//        		sampleDataGeneratorWorkerRouter.tell(data, getSelf());
//			}
//        }
        else
        {
            unhandled(message);
        }
    }
}
