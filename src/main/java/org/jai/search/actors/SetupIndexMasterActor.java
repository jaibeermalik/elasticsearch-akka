package org.jai.search.actors;

import org.jai.search.config.ElasticSearchIndexConfig;
import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.exception.IndexingException;
import org.jai.search.index.IndexProductDataService;
import org.jai.search.setup.SetupIndexService;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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

    private boolean allIndexingDone;

    private final Map<ElasticSearchIndexConfig, Boolean> indexDone = new HashMap<ElasticSearchIndexConfig, Boolean>();

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
        else if (message instanceof ElasticSearchIndexConfig)
        {
            handleIndexCompletionMessage(message);
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

    private void handleIndexCompletionMessage(final Object message)
    {
        indexDone.put((ElasticSearchIndexConfig) message, true);
        updateIndexDoneState();
    }

    private void handleException(final Object message)
    {
        // TODO check if needs to be handled differently.
        final Exception ex = (Exception) message;
        if (ex instanceof IndexingException)
        {
            indexDone.put(((IndexingException) ex).getIndexConfig(), true);
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
        // else if (IndexingMessage.INDEX_DONE.equals(indexingMessage))
        // {
        // totalToIndexDone++;
        // updateIndexDoneState();
        // }
        else
        {
            unhandled(message);
        }
    }

    private void updateIndexDoneState()
    {
        boolean isAllIndexDone = true;
        for (final Entry<ElasticSearchIndexConfig, Boolean> entry : indexDone.entrySet())
        {
            if (!entry.getValue())
            {
                isAllIndexDone = false;
            }
        }
        if (isAllIndexDone)
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
            indexDone.clear();
            // TODO as it is single instance, need not to stop it.
            // getContext().stop(getSelf());
            // TODO: check when the alising should be changed.
        }
    }

    private void setupIndicesForAll()
    {
        for (final ElasticSearchIndexConfig config : ElasticSearchIndexConfig.values())
        {
            workerRouter.tell(config, getSelf());
            indexDone.put(config, false);
        }
    }
}
