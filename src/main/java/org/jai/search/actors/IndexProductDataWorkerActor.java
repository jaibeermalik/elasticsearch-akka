package org.jai.search.actors;

import org.jai.search.index.IndexProductDataService;
import org.jai.search.model.IndexConfig;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class IndexProductDataWorkerActor extends UntypedActor
{
    final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private final IndexProductDataService indexProductDataService;

    public IndexProductDataWorkerActor(final IndexProductDataService indexProductDataService)
    {
        this.indexProductDataService = indexProductDataService;
    }

    @Override
    public void onReceive(final Object message)
    {
        LOG.debug("Worker Actor message for IndexProductDataWorkerActor is:" + message);
        if (message instanceof IndexConfig)
        {
            final IndexConfig indexConfig = (IndexConfig) message;
            indexProductDataService.indexProduct(indexConfig.getConfig(), indexConfig.getProduct());
            indexConfig.indexDone(true);
            getSender().tell(indexConfig, getSelf());
        }
        else
        {
            unhandled(message);
        }
    }
}
