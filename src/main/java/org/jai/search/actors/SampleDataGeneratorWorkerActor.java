package org.jai.search.actors;

import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.model.IndexConfig;
import org.jai.search.model.Product;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class SampleDataGeneratorWorkerActor extends UntypedActor
{
    final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private final SampleDataGeneratorService sampleDataGenerator;

    public SampleDataGeneratorWorkerActor(final SampleDataGeneratorService sampleDataGenerator)
    {
        this.sampleDataGenerator = sampleDataGenerator;
    }

    @Override
    public void onReceive(final Object message)
    {
        LOG.debug("Worker Actor message for SampleDataGeneratorWorkerActor is:" + message);
        if (message instanceof IndexConfig)
        {
            final IndexConfig indexConfig = (IndexConfig) message;
            final Product product = sampleDataGenerator.generateProductSampleDataFor(indexConfig.getProductId());
            indexConfig.product(product);
            getSender().tell(indexConfig, getSelf());
        }
        else
        {
            unhandled(message);
        }
    }
}
