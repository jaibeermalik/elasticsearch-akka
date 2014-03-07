package org.jai.search.actors;

import akka.actor.{ActorSystem, Props, Extension}
import org.springframework.context.ApplicationContext
/**
 * The Extension implementation.
 */
class SpringExtentionImplScala extends Extension {
  var applicationContext: ApplicationContext = _

  /**
   * Used to initialize the Spring application context for the extension.
   * @param applicationContext
   */
  def initialize(implicit applicationContext: ApplicationContext) = {
    this.applicationContext = applicationContext
    this
  }

  /**
   * Create a Props for the specified actorBeanName using the
   * SpringActorProducer class.
   *
   * @param actorBeanName  The name of the actor bean to create Props for
   * @return a Props that will create the named actor bean using Spring
   */
  def props(actorBeanName: String): Props = {
    Props(classOf[SpringActorProducerScala], applicationContext, actorBeanName)
  }

  def getAppContext() : ApplicationContext = {
    this.applicationContext
  }
}

object SpringExtentionImplScala {

  def apply(system : ActorSystem) (implicit ctx: ApplicationContext ) :  SpringExtentionImplScala =  SpringExtensionScala().get(system).initialize
  
//  def getAppContext() : ApplicationContext  = SpringExtensionScala().getAppContext()
}

