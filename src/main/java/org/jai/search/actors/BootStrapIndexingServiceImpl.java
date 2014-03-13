package org.jai.search.actors;

import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.setup.SetupIndexService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

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
        logger.info("Starting index preparation for {}", IndexingMessage.REBUILD_ALL_INDICES);
        setupIndexMasterActor.tell(IndexingMessage.REBUILD_ALL_INDICES, null);
        final Timeout timeout = new Timeout(Duration.create(60, "seconds"));
        Future<Object> future = Patterns.ask(setupIndexMasterActor, IndexingMessage.REBUILD_ALL_INDICES_DONE, timeout);
        try
        {
            while (!(Boolean) Await.result(future, timeout.duration()))
            {
                future = Patterns.ask(setupIndexMasterActor, IndexingMessage.REBUILD_ALL_INDICES_DONE, timeout);
                System.out.println("Got back " + false);
                Thread.sleep(1000);
            }
            System.out.println("Got back " + true);
        }
        catch (final Exception e)
        {
            System.err.println("Failed getting result: " + e.getMessage());
        }
    }
}
