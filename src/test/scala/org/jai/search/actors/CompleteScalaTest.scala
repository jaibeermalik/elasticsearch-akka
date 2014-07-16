package org.jai.search.actors

import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import com.typesafe.config.ConfigFactory
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.actor.Props
import org.jai.search.setup.SetupIndexService
import org.jai.search.data.SampleDataGeneratorService
import org.jai.search.index.IndexProductDataService

//class (_system  extends AssertionsForJUnit{
class CompleteScalaTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val applicationContext: ApplicationContext = new ClassPathXmlApplicationContext("applicationContext-elasticsearch.xml");
  
  override def beforeAll
  {
    println("Actor System used: " + system.name) ;
  }
  def this() = 
    {
//      this(new ClassPathXmlApplicationContext("applicationContext-elasticsearch.xml").getBean(classOf[ActorSystem]));

//    this(ActorSystem("Dummy"));
//    system = new ClassPathXmlApplicationContext("applicationContext-elasticsearch.xml").getBean("appConfigurationScala", classOf[AppConfigurationScala]).actorSystemScala
    this(ActorSystem("SearchIndexingSystemScala", ConfigFactory.load().getConfig("SearchIndexingSystemScala")));
    }
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "Rebuild Indexing" must {

    "rebuild all indices" in {
      val appConfing: AppConfigurationScala = new AppConfigurationScala()
      appConfing.actorSystemScala = system;
      var master = appConfing.indexDataMasterActorScala(applicationContext)
      
      val bootStrapIndexServiceScala = applicationContext.getBean(classOf[BootStrapIndexServiceScala]);
      bootStrapIndexServiceScala.prepareAllIndices(master);
    }

  }
}