package org.jai.search.actors;

import org.jai.search.config.ElasticSearchIndexConfig;
import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.exception.IndexingException;
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
            handleIndexingStatusCheck(message);
        }
        else if (message instanceof Exception)
        {
            handleException(message);
        }
        else
        {
            unhandled(message);
        }
    }

    private void handleException(final Object message)
    {
        // TODO check if needs to be handled differently.
        final Exception ex = (Exception) message;
        if (ex instanceof IndexingException)
        {
            totalToIndexDone++;
            updateIndexDoneState();
        }
        else
        {
            unhandled(message);
        }
    }

    private void handleIndexingStatusCheck(final Object message)
    {
        final IndexingMessage indexingMessage = (IndexingMessage) message;
        if (IndexingMessage.REBUILD_ALL_INDICES.equals(indexingMessage))
        {
            setupIndicesForAll();
        }
        else if (IndexingMessage.REBUILD_ALL_INDICES_DONE.equals(indexingMessage))
        {
            returnAllIndicesCurrentStateAndReset();
        }
        else if (IndexingMessage.INDEX_DONE.equals(indexingMessage))
        {
            totalToIndexDone++;
            updateIndexDoneState();
        }
        else
        {
            unhandled(message);
        }
    }

    private void updateIndexDoneState()
    {
        if (totalToIndexDone == totalToIndex)
        {
            allIndexingDone = true;
        }
    }

    private void returnAllIndicesCurrentStateAndReset()
    {
        LOG.debug("Master Actor message received for DONE check, status is:" + allIndexingDone);
        getSender().tell(allIndexingDone, getSelf());
        // Reset current state
        if (allIndexingDone)
        {
            allIndexingDone = false;
            totalToIndex = 0;
            totalToIndexDone = 0;
            // TODO as it is single instance, need not to stop it.
            // getContext().stop(getSelf());
        }
    }

    private void setupIndicesForAll()
    {
        for (final ElasticSearchIndexConfig config : ElasticSearchIndexConfig.values())
        {
            workerRouter.tell(config, getSelf());
            totalToIndex++;
        }
    }
}
