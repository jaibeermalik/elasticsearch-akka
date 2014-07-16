package org.jai.search.actors

import org.jai.search.config.ElasticSearchIndexConfig
import org.jai.search.config.IndexDocumentType
import org.jai.search.data.SampleDataGeneratorService
import org.jai.search.exception.DocumentTypeIndexingException
import org.jai.search.exception.IndexingException
import org.jai.search.index.IndexProductDataService
import org.jai.search.setup.SetupIndexService
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.UntypedActor
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.routing.FromConfig
import org.slf4j.LoggerFactory
import akka.actor.Actor
import scala.collection.mutable.Map

class SetupIndexWorkerActorScala(private val setupIndexService: SetupIndexService, sampleDataGeneratorService: SampleDataGeneratorService, indexProductDataService: IndexProductDataService) extends Actor {

  val LOG = LoggerFactory.getLogger(classOf[SetupIndexWorkerActorScala])

  private var config: ElasticSearchIndexConfig = _

  private var newIndexName: String = _

  private val documentTypesDone: Map[IndexDocumentType, Boolean] = scala.collection.mutable.Map();

  private val workerRouter = context.actorOf(Props.create(classOf[SetupDocumentTypeWorkerActorScala], sampleDataGeneratorService, indexProductDataService).withDispatcher("setupDocumentTypeWorkerActorDispatcherScala").withRouter(new FromConfig()), "setupDocumentTypeWorkerActorScala")

  def receive = {
    case message : AnyRef =>
    LOG.debug("Worker Actor message for SetupIndexWorkerActor Scala is:" + message)
    try {
      if (message.isInstanceOf[ElasticSearchIndexConfig]) {
        handleEachIndex(message)
      } else if (message.isInstanceOf[IndexDocumentType]) {
        handleIndexDocumentType(message)
      } else if (message.isInstanceOf[Exception]) {
        handleException(message)
      } else {
        unhandled(message)
      }
    } catch {
      case ex: Exception => {
        val errorMessage = "Error occurred while indexing: " + message
        LOG.error(errorMessage, ex)
        val indexingException = new IndexingException(config, errorMessage, ex)
        sendMessageToParent(indexingException)
      }
    }
  }

  private def handleIndexDocumentType(message: AnyRef) {
    val indexDocumentType = message.asInstanceOf[IndexDocumentType]
    documentTypesDone.put(indexDocumentType, true)
    updateStateAndNotifyParentIfAllDone()
  }

  private def handleException(message: AnyRef) {
    val ex = message.asInstanceOf[Exception]
    if (ex.isInstanceOf[DocumentTypeIndexingException]) {
      documentTypesDone.put(ex.asInstanceOf[DocumentTypeIndexingException].getIndexDocumentType, true)
      updateStateAndNotifyParentIfAllDone()
    } else {
      unhandled(message)
    }
  }

  private def handleEachIndex(message: AnyRef) {
    LOG.debug("Worker Actor message for initial config is  received")
    config = message.asInstanceOf[ElasticSearchIndexConfig]
    newIndexName = setupIndexService.createNewIndex(config)
    if (!Option(newIndexName).getOrElse("").isEmpty)
    {
        indexDocumentType(config, IndexDocumentType.PRODUCT)
        indexDocumentType(config, IndexDocumentType.PRODUCT_PROPERTY)
        indexDocumentType(config, IndexDocumentType.PRODUCT_GROUP)
    }
  }

  private def indexDocumentType(config: ElasticSearchIndexConfig, indexDocumentType: IndexDocumentType) {
    workerRouter.tell(new IndexDocumentTypeMessageVO().config(config).documentType(indexDocumentType).newIndexName(newIndexName), self)
    documentTypesDone.put(indexDocumentType, false)
  }

  private def updateStateAndNotifyParentIfAllDone() {
    var isAlldocumentTypeDone = true
    for ((key, value) <- documentTypesDone) {
      LOG.debug("Total indexing stats are: documentType: {}, DocumentTypesStatus: {}", Array(key, value))
      if (!value) {
        isAlldocumentTypeDone = false
      }
    }
    if (isAlldocumentTypeDone) {
      setupIndexService.replaceAlias(newIndexName, config.getIndexAliasName)
      sendMessageToParent(config)
      LOG.debug("All indexing done for the index: {} {}", Array(newIndexName, config))
      documentTypesDone.clear()
      stopTheActor()
    }
  }

  private def sendMessageToParent(message: AnyRef) {
    context.actorSelection("../../").tell(message, null)
  }

  private def stopTheActor() {
  }
}
