package org.jai.search.actors;

import scala.beans.BeanProperty
import org.jai.search.data.SampleDataGeneratorService
import org.jai.search.index.IndexProductDataService
import org.jai.search.setup.SetupIndexService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import com.typesafe.config.ConfigFactory
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import javax.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.scala.context.function.FunctionalConfiguration
import org.springframework.context.annotation.Bean

class AppConfigurationScala
{

  def LOG = LoggerFactory.getLogger(classOf[AppConfigurationScala])

  var applicationContext: ApplicationContext = _
  var actorSystemScala: ActorSystem = _
  
  def setActorSystemScala = {
    actorSystemScala = ActorSystem("SearchIndexingSystemScala", ConfigFactory.load().getConfig("SearchIndexingSystemScala"))
    LOG.debug("Actor system scala created with details" + actorSystemScala);
  }

  def indexDataMasterActorScala(applicationContext: ApplicationContext) = {
    this.applicationContext = applicationContext;
    var setupIndexService = applicationContext.getBean(classOf[SetupIndexService]);
        var sampleDataGeneratorService = applicationContext.getBean(classOf[SampleDataGeneratorService]);
        var indexProductData = applicationContext.getBean(classOf[IndexProductDataService]);
      var master: ActorRef = actorSystemScala.actorOf(Props.create(classOf[SetupIndexMasterActorScala], setupIndexService, sampleDataGeneratorService, indexProductData)
                .withDispatcher("setupIndexMasterActorDispatchScala"), "setupIndexMasterActorScala");
    master
  }

}