package org.jai.search.actors

import scala.collection.mutable.Map

import org.apache.commons.lang.time.StopWatch
import org.jai.search.config.ElasticSearchIndexConfig
import org.jai.search.data.SampleDataGeneratorService
import org.jai.search.exception.IndexingException
import org.jai.search.index.IndexProductDataService
import org.jai.search.setup.SetupIndexService
import org.slf4j.LoggerFactory

import akka.actor.Actor
import akka.actor.Props
import akka.routing.FromConfig

class SetupIndexMasterActorScala(setupIndexService: SetupIndexService, sampleDataGeneratorService: SampleDataGeneratorService, indexProductDataService: IndexProductDataService) extends Actor {

  val LOG = LoggerFactory.getLogger(classOf[SetupIndexMasterActorScala])

  private val workerRouter = context.actorOf(Props.create(classOf[SetupIndexWorkerActorScala], setupIndexService, sampleDataGeneratorService, indexProductDataService).withDispatcher("setupIndexWorkerActorDispatcherScala").withRouter(new FromConfig()), "setupIndexWorkerActorScala")

  private var allIndexingDone: Boolean = _

  private var isRebuildInProgress: Boolean = _

  private var startTime : Long = _

  private val indexDone: Map[ElasticSearchIndexConfig, Boolean] = scala.collection.mutable.Map()

  def receive = {
    case message: AnyRef =>
      LOG.debug("Master Actor message received is:" + message)
      if (message.isInstanceOf[IndexingMessage]) {
        handleIndexingMessage(message)
      } else if (message.isInstanceOf[ElasticSearchIndexConfig]) {
        handleIndexCompletionMessage(message)
      } else if (message.isInstanceOf[Exception]) {
        handleException(message)
      } else {
        unhandled(message)
      }
  }

  private def handleIndexCompletionMessage(message: AnyRef) {
    indexDone.put(message.asInstanceOf[ElasticSearchIndexConfig], true)
    updateIndexDoneState()
  }

  private def handleException(message: AnyRef) {
    val ex = message.asInstanceOf[Exception]
    if (ex.isInstanceOf[IndexingException]) {
      indexDone += ex.asInstanceOf[IndexingException].getIndexConfig -> true
      updateIndexDoneState()
    } else {
      unhandled(message)
    }
  }

  private def handleIndexingMessage(message: AnyRef) {
    val indexingMessage = message.asInstanceOf[IndexingMessage]
    if (IndexingMessage.REBUILD_ALL_INDICES == indexingMessage) {
      handleIndexingRebuildMessage(message)
    } else if (IndexingMessage.REBUILD_ALL_INDICES_DONE == indexingMessage) {
      returnAllIndicesCurrentStateAndReset()
    } else {
      unhandled(message)
    }
  }

  private def handleIndexingRebuildMessage(message: AnyRef) {
    if (!isRebuildInProgress) {
      startTime = compat.Platform.currentTime
    }
    if (allIndexingDone && isRebuildInProgress) {
      if (compat.Platform.currentTime - startTime > 5 * 60 * 1000) {
        isRebuildInProgress = false
        startTime = 0
      }
    }
    if (isRebuildInProgress) {
      LOG.error("Rebuilding is already in progress, ignoring another rebuild message: {}", message)
    } else {
      isRebuildInProgress = true
      setupIndicesForAll()
    }
  }

  private def updateIndexDoneState() {
    var isAllIndexDone = true
    for ((key, value) <- indexDone) {
      LOG.debug("Indexing setup current status is index: {} status: {}", Array(key, value))
      if (!value) {
        isAllIndexDone = false
      }
    }
    if (isAllIndexDone) {
      allIndexingDone = true
    }
  }

  private def returnAllIndicesCurrentStateAndReset() {
    LOG.debug("Master Actor message received for DONE check, status is:" + allIndexingDone)
    sender.tell(allIndexingDone, self)
    if (allIndexingDone) {
      LOG.debug("Indexing setup finished for all indices!")
      allIndexingDone = false
      indexDone.clear()
      isRebuildInProgress = false
//      stopWatch.reset()
      startTime =0
    }
  }

  private def setupIndicesForAll() {
    LOG.debug("Starting fresh Rebuilding of indices!")
    for (config <- ElasticSearchIndexConfig.values) {
      workerRouter.tell(config, self)
      indexDone.put(config, false)
    }
  }
}
