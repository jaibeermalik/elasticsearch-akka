package org.jai.search.actors;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

public class IndexDataWorkerActor extends UntypedActor
{
	private static final Logger logger = LoggerFactory.getLogger(IndexDataWorkerActor.class);
	
    public void onReceive(Object message)
    {
    	logger.debug("Worker Actor message is:" + message);
    	
        if (message instanceof IndexRequestBuilder)
        {
        	IndexRequestBuilder work = (IndexRequestBuilder) message;
        	IndexResponse result = indexDocument(work);
        
        	getSender().tell(result, getSelf());
        }
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
