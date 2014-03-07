package org.jai.search.actors;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.jai.search.query.ProductQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.util.Assert;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinPool;

//@Named(value="indexDataMasterActor")
//@Scope("prototype")
public class IndexDataMasterActor extends UntypedActor 
{
	private static final Logger logger = LoggerFactory.getLogger(IndexDataMasterActor.class);
	
	private int nrOfMessages;
    private int nrOfResults;
 
    private final ActorRef listener;
    private final ActorRef workerRouter;
    
    private final ProductQueryService productQueryService;
    
//    public IndexDataMasterActor() 
//    {
//    	listener = getContext().actorOf(Props.create(IndexResponseListenerActor.class), "indexResponseListener");
//    	workerRouter = getContext().actorOf(Props.create(IndexDataWorkerActor.class).withRouter(new RoundRobinPool(10)), "workerRouter");
//    }
    
	public IndexDataMasterActor(final int nrOfWorkers, final ProductQueryService productQueryService) 
	{
		this.productQueryService = productQueryService;
		listener = getContext().actorOf(Props.create(IndexResponseListenerActor.class), "indexResponseListener");
		workerRouter = getContext().actorOf(Props.create(IndexDataWorkerActor.class).withRouter(new RoundRobinPool(nrOfWorkers)), "workerRouter");
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
        
    	if (message instanceof List) 
        {
    		List<IndexRequestBuilder> requests = (List<IndexRequestBuilder>) message;
    		nrOfMessages = nrOfMessages + requests.size();
    	
    		
    		for (int start = 0; start < requests.size(); start++) 
    		{
    			IndexRequestBuilder requestBuilder = requests.get(start);
    			logger.debug("Passing message from master to worker is:" + requestBuilder);
				
    			workerRouter.tell(requestBuilder, getSelf());
    		}
        }
        else if (message instanceof IndexResponse) 
          {
            IndexResponse result = (IndexResponse) message;

            logger.debug("Received response message by master from worker is:" + result);
         
            // Send the result to the listener
            listener.tell(result, getSelf());

            nrOfResults += 1;
            // Stops this actor and all its supervised children
            //TODO: check stop part
            Assert.notNull(productQueryService, "Spring service should not be null!");
            
            if(nrOfResults == nrOfMessages)
            {
//              getContext().stop(getSelf());
            }
          }
        else 
        {
            unhandled(message);
        }
    }
}
