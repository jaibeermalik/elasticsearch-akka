package org.jai.search.actors;

import scala.beans.BeanProperty
import org.elasticsearch.action.index.IndexRequestBuilder
import org.elasticsearch.action.index.IndexResponse
import org.jai.search.actors.SpringExtensionScala._
import akka.actor.Actor
import akka.event.Logging
import org.slf4j.LoggerFactory

class IndexResponseListenerActorScala extends Actor {
//  val log = Logging(context.system, this)
  val log = LoggerFactory.getLogger(classOf[IndexResponseListenerActorScala])

  def receive = {
    case response: IndexResponse => log.debug("IndexDataWorkerActorScala: received request message" + response)
  }
}
