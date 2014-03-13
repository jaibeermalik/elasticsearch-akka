package org.jai.search.actors;

import org.jai.search.config.ElasticSearchIndexConfig;
import org.jai.search.config.IndexDocumentType;
import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.exception.DocumentTypeIndexingException;
import org.jai.search.exception.IndexingException;
import org.jai.search.index.IndexProductDataService;
import org.jai.search.setup.SetupIndexService;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;

public class SetupIndexWorkerActor extends UntypedActor
{
    final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private final SetupIndexService setupIndexService;

    private int totalDocumentTypesToIndex = 0;

    private int totalDocumentTypesToIndexDone = 0;

    private final ActorRef workerRouter;

    public SetupIndexWorkerActor(final SetupIndexService setupIndexService, final SampleDataGeneratorService sampleDataGeneratorService,
            final IndexProductDataService indexProductDataService)
    {
        this.setupIndexService = setupIndexService;
        workerRouter = getContext().actorOf(
                Props.create(SetupDocumentTypeWorkerActor.class, sampleDataGeneratorService, indexProductDataService)
                        .withDispatcher("setupDocumentTypeWorkerActorDispatcher").withRouter(new FromConfig()),
                "setupDocumentTypeWorkerActor");
    }

    @Override
    public void onReceive(final Object message)
    {
        LOG.debug("Worker Actor message for SetupIndexWorkerActor is:" + message);
        try
        {
            // message from master actor
            if (message instanceof ElasticSearchIndexConfig)
            {
                handleEachIndex(message);
            }
            else if (message instanceof IndexingMessage)
            {
                handleIndexingStatusCheckForEachIndex(message);
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
        catch (final Exception e)
        {
            // TODO: check if it needs to be handled different, not sure how much indexing for index or types is already done.
            final IndexingException indexingException = new IndexingException(e);
            LOG.error("Error occurred while indexing: {}", message);
            totalDocumentTypesToIndexDone++;
            sendMessageToParent(indexingException);
        }
    }

    private void handleException(final Object message)
    {
        // TODO Auto-generated method stub
        final Exception ex = (Exception) message;
        if (ex instanceof DocumentTypeIndexingException)
        {
            totalDocumentTypesToIndexDone++;
            updateStateAndNotifyParentIfAllDone();
        }
        else
        {
            unhandled(message);
        }
    }

    private void handleIndexingStatusCheckForEachIndex(final Object message)
    {
        final IndexingMessage indexingMessage = (IndexingMessage) message;
        if (IndexingMessage.DOCUMENTTYPE_DONE.equals(indexingMessage))
        {
            totalDocumentTypesToIndexDone++;
            updateStateAndNotifyParentIfAllDone();
        }
        else
        {
            unhandled(message);
        }
    }

    private void handleEachIndex(final Object message)
    {
        LOG.debug("Worker Actor message for initial config is  received");
        final ElasticSearchIndexConfig config = (ElasticSearchIndexConfig) message;
        setupIndexService.reCreateIndex(config);
        setupIndexService.updateIndexDocumentTypeMappings(config);
        // Loop through all document types and index relevant products for those.
        // eg. you want to index products/product group/specifications separately.
        // This only works if no parent child stuff other order indexing accordingly.

        // Index products
        indexDocumentType(config, IndexDocumentType.PRODUCT);
        // Index product property
        indexDocumentType(config, IndexDocumentType.PRODUCT_PROPERTY);
        // Index product group
        indexDocumentType(config, IndexDocumentType.PRODUCT_GROUP);
    }

    private void indexDocumentType(final ElasticSearchIndexConfig config, IndexDocumentType indexDocumentType)
    {
        workerRouter.tell(new IndexDocumentTypeMessageVO().config(config).documentType(indexDocumentType), getSelf());
        totalDocumentTypesToIndex++;
    }

    private void updateStateAndNotifyParentIfAllDone()
    {
        LOG.debug("Total indexing stats are: totalDocumentTypesToIndex: {}, totalDocumentTypesToIndexDone: {}", new Object[] {
                totalDocumentTypesToIndex, totalDocumentTypesToIndexDone });
        if (totalDocumentTypesToIndex == totalDocumentTypesToIndexDone)
        {
            LOG.debug("Worker Actor message for indexing done for all products!");
            sendMessageToParent(IndexingMessage.INDEX_DONE);
            totalDocumentTypesToIndex = 0;
            totalDocumentTypesToIndexDone = 0;
            stopTheActor();
        }
    }

    private void sendMessageToParent(final Object message)
    {
        // Find parent actor in the hierarchy.
        // akka://SearchIndexingSystem/user/setupIndexMasterActor/setupIndexWorkerActor/$a
        getContext().actorSelection("../../").tell(message, null);
    }

    private void stopTheActor()
    {
        // Stop the actors
        //TODO: stopping this won't restart automatically, check it.
//        getContext().stop(getSelf());
    }
}
