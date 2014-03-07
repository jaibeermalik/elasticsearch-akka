package org.jai.search.actors

import scala.beans.BeanProperty
import scala.collection.Seq

import org.elasticsearch.action.index.IndexRequestBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

import akka.actor.ActorRef
/**
 * Index data service.
 */

trait IndexDataActorSystemHandlerServiceScala {
  def handleIndexRequests(requests: Seq[IndexRequestBuilder])
}

@Service
class IndexDataActorSystemHandlerServiceImplScala extends IndexDataActorSystemHandlerServiceScala {

  val log = LoggerFactory.getLogger(classOf[IndexDataActorSystemHandlerServiceImplScala])

  @BeanProperty var indexDataMasterActorScala: ActorRef = null

  def handleIndexRequests(requests: Seq[IndexRequestBuilder]) = {
    log.debug("Passing all requests to master actor!");
    indexDataMasterActorScala.tell(requests, null)
  }
}