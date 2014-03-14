package org.jai.search.actors;

import org.jai.search.config.IndexDocumentType;
import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.exception.DocumentTypeDataGenerationException;

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
            final IndexDocumentTypeMessageVO indexDocumentTypeMessageVO = (IndexDocumentTypeMessageVO) message;
            try
            {
                if (indexDocumentTypeMessageVO.getIndexDocumentType().equals(IndexDocumentType.PRODUCT))
                {
                    generateData(indexDocumentTypeMessageVO, sampleDataGenerator.generateProductsSampleData().size());
                }
                else if (indexDocumentTypeMessageVO.getIndexDocumentType().equals(IndexDocumentType.PRODUCT_PROPERTY))
                {
                    generateData(indexDocumentTypeMessageVO, sampleDataGenerator.generateProductPropertySampleData().size());
                }
                else if (indexDocumentTypeMessageVO.getIndexDocumentType().equals(IndexDocumentType.PRODUCT_GROUP))
                {
                    generateData(indexDocumentTypeMessageVO, sampleDataGenerator.generateProductGroupSampleData().size());
                }
                else
                {
                    unhandled(message);
                }
            }
            catch (final Exception ex)
            {
                final String errorMessage = "Error occurred while generating data for message" + message;
                LOG.error(ex, errorMessage);
                final DocumentTypeDataGenerationException dataGenerationException = new DocumentTypeDataGenerationException(
                        indexDocumentTypeMessageVO.getIndexDocumentType(), errorMessage, ex);
                getSender().tell(dataGenerationException, getSelf());
            }
        }
        else
        {
            unhandled(message);
        }
    }

    private void generateData(final IndexDocumentTypeMessageVO indexDocumentTypeMessageVO, final int size)
    {
        for (int i = 1; i <= size; i++)
        {
            final IndexDocumentVO indexDocumentVO = new IndexDocumentVO().config(indexDocumentTypeMessageVO.getConfig())
                    .documentType(indexDocumentTypeMessageVO.getIndexDocumentType())
                    .newIndexName(indexDocumentTypeMessageVO.getNewIndexName()).documentId(Long.valueOf(i));
            getSender().tell(indexDocumentVO, getSelf());
        }
    }
}
