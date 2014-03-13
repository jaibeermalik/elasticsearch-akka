package org.jai.search.actors;

import org.jai.search.config.IndexDocumentType;
import org.jai.search.exception.IndexDataException;
import org.jai.search.index.IndexProductDataService;

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
        // LOG.debug("Worker Actor message for IndexProductDataWorkerActor is:" + message);
        if (message instanceof IndexDocumentVO)
        {
            try
            {
                final IndexDocumentVO indexDocumentVO = (IndexDocumentVO) message;
                if (indexDocumentVO.getDocumentType().equals(IndexDocumentType.PRODUCT))
                {
//                    if (indexDocumentVO.getDocumentId().intValue() == 36)
//                    {
//                        throw new RuntimeException("blah blah");
//                    }
                    indexProductDataService.indexProduct(indexDocumentVO.getConfig(), indexDocumentVO.getProduct());
                    indexDocumentVO.indexDone(true);
                    getSender().tell(indexDocumentVO, getSelf());
                }
                else if (indexDocumentVO.getDocumentType().equals(IndexDocumentType.PRODUCT_PROPERTY))
                {
                    indexProductDataService.indexProductPropterty(indexDocumentVO.getConfig(), indexDocumentVO.getProductProperty());
                    indexDocumentVO.indexDone(true);
                    getSender().tell(indexDocumentVO, getSelf());
                }
                else if (indexDocumentVO.getDocumentType().equals(IndexDocumentType.PRODUCT_GROUP))
                {
                    indexProductDataService.indexProductGroup(indexDocumentVO.getConfig(), indexDocumentVO.getProductGroup());
                    indexDocumentVO.indexDone(true);
                    getSender().tell(indexDocumentVO, getSelf());
                }
                else
                {
                    unhandled(message);
                }
            }
            catch (final Exception e)
            {
                LOG.error("Error occured while indexing document data for message: {}", message);
                final IndexDataException indexDataException = new IndexDataException(e);
                getSender().tell(indexDataException, getSelf());
            }
        }
        else
        {
            unhandled(message);
        }
    }
}
