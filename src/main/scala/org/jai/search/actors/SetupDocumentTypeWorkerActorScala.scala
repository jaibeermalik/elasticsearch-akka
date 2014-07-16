package org.jai.search.actors

import scala.reflect.BeanProperty

import org.jai.search.config.IndexDocumentType
import org.jai.search.data.SampleDataGeneratorService
import org.jai.search.exception.DocumentGenerationException
import org.jai.search.exception.DocumentTypeDataGenerationException
import org.jai.search.exception.DocumentTypeIndexingException
import org.jai.search.exception.IndexDataException
import org.jai.search.index.IndexProductDataService
import org.slf4j.LoggerFactory

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.routing.FromConfig

class SetupDocumentTypeWorkerActorScala(sampleDataGeneratorService: SampleDataGeneratorService, indexProductDataService: IndexProductDataService) extends Actor {

  val LOG = LoggerFactory.getLogger(classOf[SetupDocumentTypeWorkerActorScala])

  private var dataGeneratorWorkerRouter: ActorRef = context.actorOf(Props.create(classOf[DataGeneratorWorkerActorScala], sampleDataGeneratorService), "dataGeneratorWorkerScala")

  private var documentGeneratorWorkerRouter: ActorRef = context.actorOf(Props.create(classOf[DocumentGeneratorWorkerActorScala], sampleDataGeneratorService).withRouter(new FromConfig()).withDispatcher("documentGenerateWorkerActorDispatcherScala"), "documentGeneratorWorkerScala")

  private var indexDocumentWorkerRouter: ActorRef = context.actorOf(Props.create(classOf[IndexProductDataWorkerActorScala], indexProductDataService).withRouter(new FromConfig()).withDispatcher("indexDocumentWorkerActorDispatcherScala"), "indexDocumentWorkerScala")

  @BeanProperty
  var totalDocumentsToIndex: Int = 0

  @BeanProperty
  var totalDocumentsToIndexDone: Int = 0

  @BeanProperty
  var indexDocumentType: IndexDocumentType = _

  private var parentActorPathString: String = "../../"

  def receive = {
    case message : AnyRef =>
    try {
      if (message.isInstanceOf[IndexDocumentTypeMessageVO]) {
        handleDocumentTypeForDataGeneration(message)
      } else if (message.isInstanceOf[java.lang.Integer]) {
        handleTotalDocumentToIndex(message)
      } else if (message.isInstanceOf[IndexDocumentVO]) {
        generateDocumentAndIndexDocument(message)
      } else if (message.isInstanceOf[Exception]) {
        handleExceptionInChildActors(message)
      } else {
        handleUnhandledMessage(message)
      }
    } catch {
      case exception: Exception => {
        val errorMessage = "Error occured while indexing document type: " + message
        LOG.error(errorMessage, exception)
        val documentTypeIndexingException = new DocumentTypeIndexingException(indexDocumentType, errorMessage, exception)
        sendMessageToParent(documentTypeIndexingException)
      }
    }
  }

  private def handleTotalDocumentToIndex(message: AnyRef) {
    totalDocumentsToIndex = message.asInstanceOf[java.lang.Integer]
    if (totalDocumentsToIndex == 0) {
      updateStateAndResetIfAllDone()
    }
  }

  private def handleExceptionInChildActors(message: AnyRef) {
    val ex = message.asInstanceOf[Exception]
    if (ex.isInstanceOf[DocumentTypeDataGenerationException]) {
      val documentTypeIndexingException = new DocumentTypeIndexingException(indexDocumentType, "Data generation failed, failing whole document type itself!", ex)
      sendMessageToParent(documentTypeIndexingException)
      resetActorState()
    } else if (ex.isInstanceOf[DocumentGenerationException]) {
      totalDocumentsToIndexDone += 1
      updateStateAndResetIfAllDone()
    } else if (ex.isInstanceOf[IndexDataException]) {
      totalDocumentsToIndexDone += 1
      updateStateAndResetIfAllDone()
    } else {
      handleUnhandledMessage(message)
    }
  }

  private def generateDocumentAndIndexDocument(message: AnyRef) {
    val indexDocumentVO = message.asInstanceOf[IndexDocumentVO]
    if (!indexDocumentVO.isIndexDone) {
      if (indexDocumentVO.getProduct == null && indexDocumentVO.getProductProperty == null && indexDocumentVO.getProductGroup == null) {
        documentGeneratorWorkerRouter.tell(indexDocumentVO, self)
      } else {
        indexDocumentWorkerRouter.tell(indexDocumentVO, self)
      }
    } else {
      totalDocumentsToIndexDone += 1
      updateStateAndResetIfAllDone()
    }
  }

  private def handleDocumentTypeForDataGeneration(message: AnyRef) {
    val indexDocumentTypeMessageVO = message.asInstanceOf[IndexDocumentTypeMessageVO]
    indexDocumentTypeMessageVO.getConfig
    indexDocumentTypeMessageVO.getIndexDocumentType
    indexDocumentType
    totalDocumentsToIndex == 0
    totalDocumentsToIndexDone == 0
    indexDocumentType = indexDocumentTypeMessageVO.getIndexDocumentType
    dataGeneratorWorkerRouter.tell(indexDocumentTypeMessageVO, self)
  }

  private def updateStateAndResetIfAllDone() {
    LOG.debug("Total indexing stats for document type are: totalProductsToIndex: {}, totalProductsToIndexDone: {}", Array(totalDocumentsToIndex, totalDocumentsToIndexDone))
    if (totalDocumentsToIndex == totalDocumentsToIndexDone) {
      LOG.debug("All products indexing done for total document types {} sending message {} to parent!", Array(indexDocumentType, indexDocumentType))
      sendMessageToParent(indexDocumentType)
      resetActorState()
      stopTheActor()
    }
  }

  private def resetActorState() {
    totalDocumentsToIndex = 0
    totalDocumentsToIndexDone = 0
    indexDocumentType = null
  }

  private def stopTheActor() {
  }

  private def sendMessageToParent(message: AnyRef) {
    context.actorSelection(parentActorPathString).tell(message, null)
  }

  private def handleUnhandledMessage(message: AnyRef) {
    LOG.error("Unhandled message encountered in SetupDocumentTypeWorkerActorScala: {}", message)
    unhandled(message)
  }

  def setDataGeneratorWorkerRouter(dataGeneratorWorkerRouter: ActorRef) {
    this.dataGeneratorWorkerRouter = dataGeneratorWorkerRouter
  }

  def setDocumentGeneratorWorkerRouter(documentGeneratorWorkerRouter: ActorRef) {
    this.documentGeneratorWorkerRouter = documentGeneratorWorkerRouter
  }

  def setIndexDocumentWorkerRouter(indexDocumentWorkerRouter: ActorRef) {
    this.indexDocumentWorkerRouter = indexDocumentWorkerRouter
  }

  def setParentActorPathString(parentActorPathString: String) {
    this.parentActorPathString = parentActorPathString
  }
}
