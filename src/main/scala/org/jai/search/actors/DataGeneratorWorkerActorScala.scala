package org.jai.search.actors

import org.jai.search.data.SampleDataGeneratorService
import org.jai.search.exception.DocumentTypeDataGenerationException
import org.slf4j.LoggerFactory

import akka.actor.Actor

class DataGeneratorWorkerActorScala(private val sampleDataGeneratorService: SampleDataGeneratorService) extends Actor {

  val LOG = LoggerFactory.getLogger(classOf[DataGeneratorWorkerActorScala])

  override def receive = {
    case message : AnyRef =>
    if (message.isInstanceOf[IndexDocumentTypeMessageVO]) {
      val indexDocumentTypeMessageVO = message.asInstanceOf[IndexDocumentTypeMessageVO]
      try indexDocumentTypeMessageVO.getIndexDocumentType.name() match {
        case "PRODUCT" => generateData(indexDocumentTypeMessageVO, sampleDataGeneratorService.generateProductsSampleData().size)
        case "PRODUCT_PROPERTY" => generateData(indexDocumentTypeMessageVO, sampleDataGeneratorService.generateProductPropertySampleData().size)
        case "PRODUCT_GROUP" => generateData(indexDocumentTypeMessageVO, sampleDataGeneratorService.generateProductGroupSampleData().size)
        case _ => handleUnhandledMessage(message)
      } catch {
        case ex: Exception => {
          val errorMessage = "Error occurred while generating data for message" + message
          LOG.error(errorMessage, ex)
          val dataGenerationException = new DocumentTypeDataGenerationException(indexDocumentTypeMessageVO.getIndexDocumentType, errorMessage, ex)
          sender().tell(dataGenerationException, self)
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

  private def generateData(indexDocumentTypeMessageVO: IndexDocumentTypeMessageVO, size: Int) {
    sender().tell(java.lang.Integer.valueOf(size), self)
    LOG.debug("Generating data for IndexDocumentTypeMessageVO: {}, for size: {}", Array(indexDocumentTypeMessageVO, size))
    var i = 1
    while (i <= size) {
      val indexDocumentVO = new IndexDocumentVO().config(indexDocumentTypeMessageVO.getConfig).documentType(indexDocumentTypeMessageVO.getIndexDocumentType).newIndexName(indexDocumentTypeMessageVO.getNewIndexName).documentId(java.lang.Long.valueOf(i))
      sender().tell(indexDocumentVO, self)
      i += 1
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    super.preRestart(reason, message)
    LOG.debug("DataGeneratorWorkerActor restarted because of reason: {}", reason)
  }
}
