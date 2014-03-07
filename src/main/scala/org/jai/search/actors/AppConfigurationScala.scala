package org.jai.search.actors;

import akka.actor.ActorSystem
import org.springframework.context.ApplicationContext
import org.springframework.scala.context.function.FunctionalConfiguration
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.beans.factory.annotation.Autowire
import org.jai.search.query.ProductQueryService
import akka.actor.Props
import scala.beans.BeanProperty
import akka.actor.ActorRef
import akka.event.Logging
import org.slf4j.LoggerFactory
import akka.actor.ActorRefFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.util.Assert
import javax.annotation.Resource
import javax.inject.Inject
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Import
import javax.annotation.PostConstruct
import org.springframework.context.annotation.DependsOn

@Configuration
class AppConfigurationScala {

  def LOG = LoggerFactory.getLogger(classOf[AppConfigurationScala])

  @BeanProperty
  @Autowired
  var applicationContext: ApplicationContext = null

  @BeanProperty
  @Qualifier(value = "applicationContext")
  @Autowired
  implicit var ctx: ApplicationContext = null

  /**
   * Actor system singleton for this application.
   */
  @Bean
  val actorSystemScala = {
    val system = ActorSystem("SearchIndexingSystemScala")
    LOG.debug("Actor system scala created with details" + system);
    system
  }

  //  val actorSystemScala = bean(name="actorSystemScala") {
  //    val system = ActorSystem("SearchIndexingSystemScala")
  //    // initialize the application context in the Akka Spring Extension
  //    SpringExtentionImplScala(system)
  //    LOG.debug("Actor system created with details" + system);
  ////    system.asInstanceOf[ActorRefFactory]
  //    system
  //  }

  @Bean
  @DependsOn(value = Array("actorSystemScala"))
  val indexDataMasterActorScala = {
    val system = actorSystemScala
    val master = system.actorOf(Props(classOf[IndexDataMasterActorScala]), "masterActorScala")
    master
  }

  @PostConstruct
  def indexDataMasterActorScalaInject = {
    assert(applicationContext != null, "App context applicationContext can't be null")
    SpringExtentionImplScala(actorSystemScala).applicationContext = applicationContext

    //    val prop = SpringExtentionImplScala(system).props("indexDataMasterActorScala")
    //    val productQueryService = applicationContext.getBean(classOf[ProductQueryService])
    //    indexDataMasterActorScala.setProductQueryService(productQueryService)
    //    indexDataMasterActorScala.asInstanceOf[IndexDataMasterActorScala].setProductQueryService(productQueryService)
    val indexDataActorSystemHandlerServiceImplScala = applicationContext.getBean(classOf[IndexDataActorSystemHandlerServiceImplScala])
    indexDataActorSystemHandlerServiceImplScala.setIndexDataMasterActorScala(indexDataMasterActorScala)
  }

  //	@Bean
  //  val indexDataMasterActorScala = bean(name="indexDataMasterActorScala") {
  //    val system = applicationContext.getBean("actorSystemScala").asInstanceOf[ActorSystem]
  //    LOG.debug("Actor system retrieved for master actor with details" + system);
  //    val productQueryService = applicationContext.getBean(classOf[ProductQueryService]);
  //    val prop = SpringExtentionImplScala(system).props("indexDataMasterActorScala")
  //    val master = system.actorOf(prop, "masterActorScala")
  //    LOG.debug("Master Actor Scala created with details" + master);
  //    master
  //  }
}