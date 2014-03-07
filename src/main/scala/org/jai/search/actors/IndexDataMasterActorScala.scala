package org.jai.search.actors;

import scala.beans.BeanProperty
import scala.Int
import org.elasticsearch.action.index.IndexRequestBuilder
import org.jai.search.actors.SpringExtensionScala._
import org.jai.search.query.ProductQueryService
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.event.Logging
import akka.routing.RoundRobinPool
import org.elasticsearch.action.index.IndexResponse
import org.slf4j.LoggerFactory

class IndexDataMasterActorScala extends Actor {
//  val log = Logging(context.system, this)
  val log = LoggerFactory.getLogger(classOf[IndexDataMasterActorScala])
  
  @BeanProperty var productQueryService: ProductQueryService = null
  val listener : ActorRef = this.context.actorOf(Props.create(classOf[IndexResponseListenerActorScala]), "indexResponseListenerScala");
  val workerRouter : ActorRef = this.context.actorOf(Props.create(classOf[IndexDataWorkerActorScala]).withRouter(new RoundRobinPool(10)), "workerRouter") 
  
//  def IndexDataMasterActorScala() = {
//	  this.listener = ;
//	  this.workerRouter = ;
//	  log.debug("IndexDataMasterActorScala: constructor object created sucessfully!")
//  }
  
//  def IndexDataMasterActorScala(productQueryService: ProductQueryService, numberOfWorkers: Int) = {
//	  this.productQueryService = productQueryService;
//	  this.listener = this.context.actorOf(Props.create(classOf[IndexResponseListenerActorScala]), "indexResponseListenerScala");
//	  this.workerRouter = this.context.actorOf(Props.create(classOf[IndexDataWorkerActorScala]).withRouter(new RoundRobinPool(numberOfWorkers)), "workerRouter");
//	  log.debug("IndexDataMasterActorScala: constructor object created sucessfully!")
//  }

  def receive = {
//    assert(workerRouter != null, "")
    case requests: Seq[IndexRequestBuilder] => 
      log.debug("IndexDataMasterActorScala: requests received, forwarding to worker" + requests)
      requests.foreach(request => workerRouter.tell(request, this.self))
    case result: IndexResponse => 
      log.debug("IndexDataMasterActorScala: response received, forwarding to listerner" + result)
      listener.tell(result, this.self)
  }
  
}
