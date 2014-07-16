package org.jai.search.actors

import org.jai.search.exception.IndexDataException
import org.jai.search.index.IndexProductDataService
import org.slf4j.LoggerFactory

import akka.actor.Actor

class IndexProductDataWorkerActorScala(private val indexProductDataService: IndexProductDataService) extends Actor {

  val LOG = LoggerFactory.getLogger(classOf[IndexProductDataWorkerActorScala])

  def receive = {
    case message : AnyRef =>
    if (message.isInstanceOf[IndexDocumentVO]) {
      try {
        val indexDocumentVO = message.asInstanceOf[IndexDocumentVO]
        indexDocumentVO.getDocumentType.name() match {
          case "PRODUCT" => 
            indexProductDataService.indexProduct(indexDocumentVO.getConfig, indexDocumentVO.getNewIndexName, indexDocumentVO.getProduct)
            indexDocumentVO.indexDone(true)
            sender.tell(indexDocumentVO, self)

          case "PRODUCT_PROPERTY" => 
            indexProductDataService.indexProductPropterty(indexDocumentVO.getConfig, indexDocumentVO.getNewIndexName, indexDocumentVO.getProductProperty)
            indexDocumentVO.indexDone(true)
            sender.tell(indexDocumentVO, self)

          case "PRODUCT_GROUP" => 
            indexProductDataService.indexProductGroup(indexDocumentVO.getConfig, indexDocumentVO.getNewIndexName, indexDocumentVO.getProductGroup)
            indexDocumentVO.indexDone(true)
            sender.tell(indexDocumentVO, self)

          case _ => handleUnhandledMessage(message)
        }
      } catch {
        case e: Exception => {
          LOG.error("Error occured while indexing document data for message:" + message, e)
          val indexDataException = new IndexDataException(e)
          sender.tell(indexDataException, self)
        }
      }
    } else {
      handleUnhandledMessage(message)
    }
  }

  private def handleUnhandledMessage(message: AnyRef) {
    LOG.error("Unhandled message encountered in DataGeneratorWorkerActor: {}", message)
    unhandled(message)
  }
}
