package org.jai.search.actors;

import org.jai.search.config.ElasticSearchIndexConfig;
import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.index.IndexProductDataService;
import org.jai.search.model.IndexConfig;
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

    private final ActorRef sampleDataGeneratorWorkerRouter;

    private final ActorRef indexProductDataWorkerRouter;

    private int totalProductsToIndex = 0;

    private int totalProductsToIndexDone = 0;

    private boolean allIndexingDone = false;

    public SetupIndexWorkerActor(final SetupIndexService setupIndexService, final SampleDataGeneratorService sampleDataGeneratorService,
            final IndexProductDataService indexProductDataService)
    {
        this.setupIndexService = setupIndexService;
        sampleDataGeneratorWorkerRouter = getContext().actorOf(
                Props.create(SampleDataGeneratorWorkerActor.class, sampleDataGeneratorService).withRouter(new FromConfig())
                        .withDispatcher("dataGenerateAndIndexWorkerActorDispatcher"), "sampleDataGeneratorWorker");
        indexProductDataWorkerRouter = getContext().actorOf(
                Props.create(IndexProductDataWorkerActor.class, indexProductDataService).withRouter(new FromConfig())
                        .withDispatcher("dataGenerateAndIndexWorkerActorDispatcher"), "indexProductDataWorker");
    }

    @Override
    public void onReceive(final Object message)
    {
        LOG.debug("Worker Actor message for SetupIndexWorkerActor is:" + message);
        // message from master actor
        if (message instanceof ElasticSearchIndexConfig)
        {
            LOG.debug("Worker Actor message for initial config is  received");
            final ElasticSearchIndexConfig config = (ElasticSearchIndexConfig) message;
            setupIndexService.reCreateIndex(config);
            setupIndexService.updateIndexDocumentTypeMappings(config);
            for (int i = 0; i < 50; i++)
            {
                final IndexConfig indexConfig = new IndexConfig().config(config).productId(Long.valueOf(i));
                sampleDataGeneratorWorkerRouter.tell(indexConfig, getSelf());
                totalProductsToIndex++;
            }
        }
        // message from data generator and indexer
        if (message instanceof IndexConfig)
        {
            final IndexConfig indexConfig = (IndexConfig) message;
            if (!indexConfig.isIndexDone() && indexConfig.getProduct() != null)
            {
                LOG.debug("Worker Actor message for indexing product id" + indexConfig.getProductId());
                indexProductDataWorkerRouter.tell(indexConfig, getSelf());
            }
            if (indexConfig.isIndexDone())
            {
                ++totalProductsToIndexDone;
                LOG.debug("Total indexing stats are: totalProductsToIndex: {}, totalProductsToIndexDone: {}", new Object[] {
                        totalProductsToIndex, totalProductsToIndexDone });
                if (totalProductsToIndex == totalProductsToIndexDone)
                {
                    LOG.debug("Worker Actor message for indexing done for all products!");
                    allIndexingDone = true;
                    totalProductsToIndex =0;
                    totalProductsToIndexDone = 0;
                    //Find parent actor in the hierarchy.
                    //akka://SearchIndexingSystem/user/setupIndexMasterActor/setupIndexWorkerActor/$a
                    getContext().actorSelection("../../").tell("DONEALL", null);
//                    getContext().parent().tell("DONEALL", null);
                }
            }
        }
//        else if (message instanceof String)
//        {
//            final String msg = (String) message;
//            if (msg.equals("DONEALL"))
//            {
//                if (allIndexingDone)
//                {
//                    LOG.debug("Master Actor message received from workers that DONEALL!");
//                    sender().tell("DONEALL", null);
//                    allIndexingDone = false;
//                }
//                else
//                {
//                    sender().tell("DONEALL_NO", null);
//                }
//            }
//        }
        else
        {
            unhandled(message);
        }
    }
}
