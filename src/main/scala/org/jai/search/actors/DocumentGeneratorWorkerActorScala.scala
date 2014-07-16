package org.jai.search.actors

import org.jai.search.data.SampleDataGeneratorService
import org.jai.search.exception.DocumentGenerationException
import org.slf4j.LoggerFactory

import akka.actor.Actor

class DocumentGeneratorWorkerActorScala(private val sampleDataGenerator: SampleDataGeneratorService) extends Actor {

  val LOG = LoggerFactory.getLogger(classOf[DocumentGeneratorWorkerActorScala])

  def receive = {
    case message : AnyRef =>
    if (message.isInstanceOf[IndexDocumentVO]) {
      try {
        val indexDocumentVO = message.asInstanceOf[IndexDocumentVO]
        indexDocumentVO.getDocumentType.name() match {
          case "PRODUCT" => 
            val product = sampleDataGenerator.generateProductSampleDataFor(indexDocumentVO.getDocumentId)
            indexDocumentVO.product(product)
            sender.tell(indexDocumentVO, null)

          case "PRODUCT_PROPERTY" => 
            val productProperty = sampleDataGenerator.generateProductPropertySampleDataFor(indexDocumentVO.getDocumentId)
            indexDocumentVO.productProperty(productProperty)
            sender.tell(indexDocumentVO, null)

          case "PRODUCT_GROUP" => 
            val productGroup = sampleDataGenerator.generateProductGroupSampleDataFor(indexDocumentVO.getDocumentId)
            indexDocumentVO.productGroup(productGroup)
            sender.tell(indexDocumentVO, null)

          case _ => handleUnhandledMessage(message)
        }
      } catch {
        case e: Exception => {
          LOG.error("Error occurred while generating document for message: " + message, e)
          val documentGenerationException = new DocumentGenerationException(e)
          sender.tell(documentGenerationException, self)
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

  override def postRestart(reason: Throwable) {
    super.postRestart(reason)
    LOG.info("Restarted because of: {}", reason.getMessage)
  }
}
