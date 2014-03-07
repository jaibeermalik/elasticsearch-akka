package org.jai.search.actors;

import org.elasticsearch.action.index.IndexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

public class IndexResponseListenerActor extends UntypedActor
{
	private static final Logger logger = LoggerFactory.getLogger(IndexResponseListenerActor.class);
	
    public void onReceive(Object message) 
    {
        if (message instanceof IndexResponse) 
        {
        	IndexResponse indexResponse = (IndexResponse) message;
        	logger.debug("Index response received by listener actor is:"+ indexResponse);
        	//TODO it should shutdown in the end, closing the spring context.
        	//getContext().system().shutdown();
        }
        else 
        {
          unhandled(message);
        }
      }
}
