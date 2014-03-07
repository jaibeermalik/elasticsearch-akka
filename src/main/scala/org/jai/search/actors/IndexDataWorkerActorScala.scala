package org.jai.search.actors;

import scala.beans.BeanProperty
import org.elasticsearch.action.index.IndexRequestBuilder
import org.elasticsearch.action.index.IndexResponse
import org.jai.search.actors.SpringExtensionScala._
import akka.actor.Actor
import akka.event.Logging
import org.slf4j.LoggerFactory

class IndexDataWorkerActorScala extends Actor {
//  val log = Logging(context.system, this)
  val log = LoggerFactory.getLogger(classOf[IndexDataWorkerActorScala])

  def receive = {
    case request: IndexRequestBuilder => sender.tell(request.execute().actionGet(), self)
    log.debug("IndexDataWorkerActorScala: received request message")
  }
}
