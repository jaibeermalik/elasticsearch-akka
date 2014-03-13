package org.jai.search.actors;

import org.jai.search.config.IndexDocumentType;
import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.exception.DataGenerationException;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class DataGeneratorWorkerActor extends UntypedActor
{
    final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private final SampleDataGeneratorService sampleDataGenerator;

    public DataGeneratorWorkerActor(final SampleDataGeneratorService sampleDataGenerator)
    {
        this.sampleDataGenerator = sampleDataGenerator;
    }

    @Override
    public void onReceive(final Object message)
    {
        // message from master actor
        if (message instanceof IndexDocumentTypeMessageVO)
        {
            try
            {
                final IndexDocumentTypeMessageVO indexDocumentTypeMessageVO = (IndexDocumentTypeMessageVO) message;
                if (indexDocumentTypeMessageVO.getIndexDocumentType().equals(IndexDocumentType.PRODUCT))
                {
                    generateProductsData(indexDocumentTypeMessageVO);
                }
                else if (indexDocumentTypeMessageVO.getIndexDocumentType().equals(IndexDocumentType.PRODUCT_PROPERTY))
                {
                    generateProductPropertyData(indexDocumentTypeMessageVO);
                }
                else if (indexDocumentTypeMessageVO.getIndexDocumentType().equals(IndexDocumentType.PRODUCT_GROUP))
                {
                    generateProductGroupData(indexDocumentTypeMessageVO);
                }
                else
                {
                    unhandled(message);
                }
            }
            catch (final Exception ex)
            {
                LOG.error("Error occurred while generating data for message {}", message);
                final DataGenerationException dataGenerationException = new DataGenerationException(ex);
                getSender().tell(dataGenerationException, getSelf());
            }
        }
        else
        {
            unhandled(message);
        }
    }

    private void generateProductGroupData(IndexDocumentTypeMessageVO indexDocumentTypeMessageVO)
    {
        final int size = sampleDataGenerator.generateProductGroupSampleData().size();
        for (int i = 1; i <= size; i++)
        {
            final IndexDocumentVO indexConfig = new IndexDocumentVO().config(indexDocumentTypeMessageVO.getConfig())
                    .documentType(indexDocumentTypeMessageVO.getIndexDocumentType()).documentId(Long.valueOf(i));
            getSender().tell(indexConfig, getSelf());
        }
    }

    private void generateProductPropertyData(final IndexDocumentTypeMessageVO indexDocumentTypeMessageVO)
    {
        final int size = sampleDataGenerator.generateProductPropertySampleData().size();
        for (int i = 1; i <= size; i++)
        {
            final IndexDocumentVO indexConfig = new IndexDocumentVO().config(indexDocumentTypeMessageVO.getConfig())
                    .documentType(indexDocumentTypeMessageVO.getIndexDocumentType()).documentId(Long.valueOf(i));
            getSender().tell(indexConfig, getSelf());
        }
    }

    private void generateProductsData(final IndexDocumentTypeMessageVO indexDocumentTypeMessageVO)
    {
        final int size = sampleDataGenerator.generateProductsSampleData().size();
        for (int i = 1; i <= size; i++)
        {
            final IndexDocumentVO indexDocumentVO = new IndexDocumentVO().config(indexDocumentTypeMessageVO.getConfig())
                    .documentType(indexDocumentTypeMessageVO.getIndexDocumentType()).documentId(Long.valueOf(i));
            getSender().tell(indexDocumentVO, getSelf());
        }
    }
}
