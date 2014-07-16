package org.jai.search.actors

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.StopWatch

import akka.actor.ActorRef
import akka.pattern.Patterns
import akka.util.Timeout

trait BootStrapIndexServiceScala
{
    def prepareAllIndices(masterActor: ActorRef);
}

@Service
class BootStrapIndexingServiceImplScala extends BootStrapIndexServiceScala {

  private val LOG = LoggerFactory.getLogger(classOf[BootStrapIndexingServiceImplScala])

  override def prepareAllIndices(setupIndexMasterActorScala: ActorRef) {
    LOG.info("Starting index preparation for {}", IndexingMessage.REBUILD_ALL_INDICES)
    setupIndexMasterActorScala.tell(IndexingMessage.REBUILD_ALL_INDICES, null)
    val timeout = new Timeout(Duration.create(10, "seconds"))
    val stopWatch = new StopWatch()
    var future = Patterns.ask(setupIndexMasterActorScala, IndexingMessage.REBUILD_ALL_INDICES_DONE, timeout)
    try {
      stopWatch.start()
      while (!Await.result(future, timeout.duration).asInstanceOf[java.lang.Boolean] && stopWatch.getTotalTimeSeconds < 5 * 60) {
        future = Patterns.ask(setupIndexMasterActorScala, IndexingMessage.REBUILD_ALL_INDICES_DONE, timeout)
        LOG.debug("Index setup status check, Got back " + false)
        Thread.sleep(100)
      }
      LOG.debug("Index setup status check, Got back " + true)
    } catch {
      case e: Exception => LOG.debug("Index setup status check, Failed getting result: " + e.getMessage)
    }
    LOG.debug("All indexing setup finished using Akka system, Enjoy!")
  }
}
