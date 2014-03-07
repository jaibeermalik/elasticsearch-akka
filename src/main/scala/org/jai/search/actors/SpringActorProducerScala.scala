package org.jai.search.actors;

import akka.actor.{Actor, IndirectActorProducer}
import org.springframework.scala.context.function. {FunctionalConfigApplicationContext => FCA }
import org.springframework.context.ApplicationContext


class SpringActorProducerScala(ctx: ApplicationContext, actorBeanName: String) extends IndirectActorProducer {

  override def produce: Actor = ctx.getBean(actorBeanName, classOf[Actor])

  override def actorClass: Class[_ <: Actor] =
    ctx.getType(actorBeanName).asInstanceOf[Class[_ <: Actor]]
}
