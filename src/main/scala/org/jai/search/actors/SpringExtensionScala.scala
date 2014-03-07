package org.jai.search.actors;

import akka.actor._

import org.springframework.context.ApplicationContext




object SpringExtensionScala {
  /**
   * The identifier used to access the SpringExtension.
   */
  def apply() : SpringExtensionScala= new SpringExtensionScala
  
//  def getAppContext() : ApplicationContext
}

class SpringExtensionScala extends AbstractExtensionId[SpringExtentionImplScala] 
{
    import SpringExtensionScala._
  /**
   * Is used by Akka to instantiate the Extension identified by this
   * ExtensionId, internal use only.
   */
  override def createExtension(system: ExtendedActorSystem) = new SpringExtentionImplScala

  /**
   * Java API: retrieve the SpringExt extension for the given system.
   */
  override def get(system: ActorSystem): SpringExtentionImplScala = super.get(system)
  
//  override def getAppContext(): ApplicationContext = super.getAppContext()

}

