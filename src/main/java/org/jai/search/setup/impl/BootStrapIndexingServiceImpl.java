package org.jai.search.setup.impl;

import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.model.ElasticSearchIndexConfig;
import org.jai.search.setup.BootStrapIndexService;
import org.jai.search.setup.SetupIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;

@Service
public class BootStrapIndexingServiceImpl implements BootStrapIndexService 
{
	private static final Logger logger = LoggerFactory.getLogger(BootStrapIndexingServiceImpl.class);

	@Autowired
	private SampleDataGeneratorService sampleDataGenerator;
	@Autowired
	private SetupIndexService setupIndexService;
	@Autowired
	private ActorRef setupIndexMasterActor;

	@Override
	public void preparingIndexes() 
	{
		for (ElasticSearchIndexConfig config : ElasticSearchIndexConfig.values()) 
		{
			logger.info("Starting index preparation for {}", config);
			setupIndexMasterActor.tell(config, null);
		}
		
		Timeout timeout = new Timeout(Duration.create(60, "seconds"));
		Future<Object> future = Patterns.ask(setupIndexMasterActor, "DONE", timeout);
		// print the result
//		    FiniteDuration duration = FiniteDuration.create(60, TimeUnit.SECONDS);
//		    Future<Object> result = ask(setupIndexMasterActor, "DONE", Timeout.durationToTimeout(duration));
		//Await.result(result, duration)
		try {
			while(!(Boolean) Await.result(future, timeout.duration()))
			{
				future = Patterns.ask(setupIndexMasterActor, "DONE", timeout);
				System.out.println("Got back " + false);
				Thread.sleep(1000);
			}
//				boolean result = ;
			System.out.println("Got back " + true);
		} catch (Exception e) {
			System.err.println("Failed getting result: " + e.getMessage());
//		      throw new Runtime;
		}
	}

}
