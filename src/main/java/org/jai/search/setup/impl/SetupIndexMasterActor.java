package org.jai.search.setup.impl;

import java.util.List;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.jai.search.actors.IndexDataWorkerActor;
import org.jai.search.actors.IndexResponseListenerActor;
import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.index.IndexProductDataService;
import org.jai.search.model.ElasticSearchIndexConfig;
import org.jai.search.model.IndexConfig;
import org.jai.search.query.ProductQueryService;
import org.jai.search.setup.SetupIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinPool;

public class SetupIndexMasterActor extends UntypedActor
{
	private static final Logger logger = LoggerFactory.getLogger(SetupIndexMasterActor.class);
	
//    private final ActorRef listener;
    private final ActorRef workerRouter;
    private int totalToIndex =0;
	private int totalToIndexDone =0;
	private boolean allIndexingDone;
//    private final ProductQueryService productQueryService;
    
//    public IndexDataMasterActor() 
//    {
//    	listener = getContext().actorOf(Props.create(IndexResponseListenerActor.class), "indexResponseListener");
//    	workerRouter = getContext().actorOf(Props.create(IndexDataWorkerActor.class).withRouter(new RoundRobinPool(10)), "workerRouter");
//    }
    
	public SetupIndexMasterActor(int nrOfIndexSetupWorkers, int nrOfDataGeneratorWorkers, int nrOfIndexDataWorkers, 
			SetupIndexService setupIndexService, SampleDataGeneratorService sampleDataGeneratorService, IndexProductDataService indexProductDataService) 
	{
//		listener = getContext().actorOf(Props.create(IndexResponseListenerActor.class), "indexResponseListener");
		workerRouter = getContext().actorOf(Props.create(SetupIndexWorkerActor.class, nrOfDataGeneratorWorkers, nrOfIndexDataWorkers, 
				setupIndexService, sampleDataGeneratorService, indexProductDataService).withRouter(new RoundRobinPool(nrOfIndexSetupWorkers)), "setupIndexworkerRouter");
	}
    
//    public IndexDataMasterActor() 
//    {
//    }
    
//    public IndexDataMasterActor(final int nrOfWorkers, ActorRef listener) 
//    {
//    	logger.debug("Creating master actor with total workers: " + nrOfWorkers);
//        
//    	this.listener = listener;
//        workerRouter = getContext().actorOf(Props.create(IndexDataWorkerActor.class).withRouter(new RoundRobinPool(nrOfWorkers)), "workerRouter");
//    }

    @SuppressWarnings("unchecked")
	@Override
    public void onReceive(Object message) throws Exception
    {
    	logger.debug("Master Actor message received is:" + message);
        
    	if (message instanceof ElasticSearchIndexConfig) 
        {
    		logger.debug("Master Actor Ref is:" + getSelf());
    		totalToIndex++;
    		ElasticSearchIndexConfig config = (ElasticSearchIndexConfig) message;
    		workerRouter.tell(config, getSelf());
//    		for (int start = 0; start < requests.size(); start++) 
//    		{
//    			IndexRequestBuilder requestBuilder = requests.get(start);
//    			logger.debug("Passing message from master to worker is:" + requestBuilder);
//				
//    		}
        }
    	else if(message instanceof String)
    	{
    		String msg = (String) message;
    		if(msg.equals("DONEALL"))
    		{
    			totalToIndexDone++;
    			logger.debug("Master Actor message received from workers that DONEALL!");
    			if(totalToIndexDone == totalToIndex)
    			{
	    			allIndexingDone = true;
    			}
    		}
    		if(msg.equals("DONE"))
    		{
    			logger.debug("Total indexing stats in Master Actor are, allIndexingDone: {}, totalToIndex: {}, totalToIndexDone: {}", 
    					new Object[]{allIndexingDone, totalToIndex, totalToIndexDone});
    			workerRouter.tell("DONEALL", getSelf());
    			logger.debug("Master Actor message received for DONE check, status is:" + allIndexingDone);
    			getSender().tell(allIndexingDone, getSelf());
    			if(allIndexingDone)
    			{
    				allIndexingDone = false;
    				totalToIndex = 0;
    				totalToIndexDone =0;
    			}
    		}
    	}
//        else if (message instanceof IndexResponse) 
//          {
//            IndexResponse result = (IndexResponse) message;
//
//            logger.debug("Received response message by master from worker is:" + result);
//          }
        else 
        {
            unhandled(message);
        }
    }
}
