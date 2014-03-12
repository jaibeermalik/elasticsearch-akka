package org.jai.search.actors;

import org.jai.search.config.ElasticSearchIndexConfig;
import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.index.IndexProductDataService;
import org.jai.search.setup.SetupIndexService;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;

public class SetupIndexMasterActor extends UntypedActor
{
    final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private final ActorRef workerRouter;

    private int totalToIndex = 0;

    private int totalToIndexDone = 0;

    private boolean allIndexingDone;

    public SetupIndexMasterActor(final SetupIndexService setupIndexService, final SampleDataGeneratorService sampleDataGeneratorService,
            final IndexProductDataService indexProductDataService)
    {
        workerRouter = getContext().actorOf(
                Props.create(SetupIndexWorkerActor.class, setupIndexService, sampleDataGeneratorService, indexProductDataService)
                        .withDispatcher("setupIndexWorkerActorDispatcher").withRouter(new FromConfig()), "setupIndexWorkerActor");
    }

    @Override
    public void onReceive(final Object message) throws Exception
    {
        LOG.debug("Master Actor message received is:" + message);
        if (message instanceof IndexingMessage)
        {
            IndexingMessage indexingMessage = (IndexingMessage) message;
            if(IndexingMessage.REBUILD_ALL_INDICES.equals(indexingMessage))
            {
                for (ElasticSearchIndexConfig config : ElasticSearchIndexConfig.values())
                {
                    workerRouter.tell(config, getSelf());
                    totalToIndex++;
                }
            }
        }
        else if (message instanceof ElasticSearchIndexConfig)
        {
            LOG.debug("Master Actor Ref is:" + getSelf());
            totalToIndex++;
            final ElasticSearchIndexConfig config = (ElasticSearchIndexConfig) message;
            workerRouter.tell(config, getSelf());
        }
        
        else if (message instanceof String)
        {
            final String inputMessageString = (String) message;
            if (inputMessageString.equals("REBUILD_ALL_INDICES"))
            {
                
            }
            
            if (inputMessageString.equals("DONEALL"))
            {
                totalToIndexDone++;
                LOG.debug("Master Actor message received from workers that DONEALL!");
                if (totalToIndexDone == totalToIndex)
                {
                    allIndexingDone = true;
                }
            }
            if (inputMessageString.equals("DONE"))
            {
//                LOG.debug("Total indexing stats in Master Actor are, allIndexingDone: {}, totalToIndex: {}, totalToIndexDone: {}",
//                        new Object[] { allIndexingDone, totalToIndex, totalToIndexDone });
//                workerRouter.tell("DONEALL", getSelf());
                LOG.debug("Master Actor message received for DONE check, status is:" + allIndexingDone);
                getSender().tell(allIndexingDone, getSelf());
                if (allIndexingDone)
                {
                    allIndexingDone = false;
                    totalToIndex = 0;
                    totalToIndexDone = 0;
                }
            }
        }
        else
        {
            unhandled(message);
        }
    }
}
