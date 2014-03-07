package org.jai.search.actors;

import java.util.List;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;


@Service
public class IndexDataActorSystemHandlerServiceImpl implements IndexDataActorSystemHandlerService
{
	private static final Logger logger = LoggerFactory.getLogger(IndexDataActorSystemHandlerServiceImpl.class);
	
//	private static final ActorRef master;
	
//	@Qualifier(value="indexDataMasterActor")
//	@Autowired
//	private ActorRef indexDataMasterActor ;//= SpringExtension.SpringExtProvider.get().props("CountingActor"), "counter");;
    
	
//	@Qualifier(value="indexDataMasterActor")
//	@Autowired
//	private ActorRef indexDataMasterActor;
//	@Autowired
//	private ActorRef indexDataMasterActor2;
	
//	@Qualifier(value="indexDataMasterActor")
//	@Autowired
//	private IndexDataMasterActor indexDataMasterActor;
	
    static
    {
        // Create an Akka system
//    	ActorSystem system = ActorSystem.create("SearchIndexingSystem");
//    	final ActorRef listener = system.actorOf(Props.create(IndexResponseListenerActor.class), "indexResponseListener");
//    	master = system.actorOf(Props.create(IndexDataMasterActor.class, 10, listener), "masterActor");
    }
    
    @Override
    public void handleIndexRequests(List<IndexRequestBuilder> requests)
    {
//    	System.out.println("find bean:" + applicationContext.getBean("productQueryService"));
    	
    	logger.debug("Passing all requests to master actor!");
        // start the calculation
        //TODO: check null
//    	ActorRef indexDataMasterActor = actorSystem.actorOf(
//    			SpringExtension.SpringExtProvider.get(actorSystem).props("indexDataMasterActor"), "indexDataMasterActor");
//    	ActorRef indexDataMasterActor = (ActorRef) applicationContext.getBean("indexDataMasterActor");
//    	indexDataMasterActor.tell(requests, null);
    	
//    	System.out.println("find bean:" + applicationContext.getBean("indexDataMasterActor"));
//    	System.out.println("find bean:" + applicationContext.getBean("indexDataMasterActor2"));
    }
}
