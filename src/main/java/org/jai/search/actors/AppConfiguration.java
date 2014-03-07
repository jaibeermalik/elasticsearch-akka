package org.jai.search.actors;

import static org.jai.search.actors.SpringExtension.SpringExtProvider;

import javax.annotation.PreDestroy;

import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.index.IndexProductDataService;
import org.jai.search.query.ProductQueryService;
import org.jai.search.setup.SetupIndexService;
import org.jai.search.setup.impl.SetupIndexMasterActor;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.util.Assert;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * The application configuration.
 */
//TODO: this works well, uncomment later
@Configuration
class AppConfiguration {

  // the application context is needed to initialize the Akka Spring Extension
  @Autowired
  private ApplicationContext applicationContext;

  /**
   * Actor system singleton for this application.
   */
  @Bean(autowire=Autowire.BY_NAME, name="actorSystem")
  public ActorRefFactory actorSystem() {
    ActorSystem system = ActorSystem.create("SearchIndexingSystem");
    Assert.notNull(applicationContext);
    // initialize the application context in the Akka Spring Extension
    SpringExtProvider.get(system).initialize(applicationContext);
    return system;
  }
  
//  @Bean(autowire=Autowire.BY_NAME, name="indexDataMasterActor")
//  public ActorRef indexDataMasterActor() 
//  {
//	  ActorSystem system = (ActorSystem) applicationContext.getBean("actorSystem");
////	  ActorRefFactory system = (ActorRefFactory) applicationContext.getBean("actorSystem");
////	  system.systemImpl().
//    ProductQueryService productQueryService = (ProductQueryService) applicationContext.getBean(ProductQueryService.class);
////    final ActorRef listener = system.actorOf(Props.create(IndexResponseListenerActor.class), "indexResponseListener");
////    ActorRef master = system.actorOf(Props.create(IndexDataMasterActor.class, 10, listener), "masterActor");
//    ActorRef master = system.actorOf(Props.create(IndexDataMasterActor.class, 10, productQueryService), "masterActor");
//    
////    ActorRef master = system.actorOf(SpringExtProvider.get(system).props("indexDataMasterActor"), "indexDataMasterActor");
//	return master;
//  }
//  
//  @Bean(autowire=Autowire.BY_NAME, name="indexDataMasterActor2")
//  public ActorRef indexDataMasterActor2() 
//  {
//    ActorSystem system = applicationContext.getBean(ActorSystem.class);
//    return system.actorOf(Props.create(IndexDataMasterActor.class, 10, null), "masterActor2");
//  }
  
//  @Bean(autowire=Autowire.BY_NAME, name="setupIndexMasterActor")
//  @Scope("prototype")
//  @DependsOn(value={"actorSystem"})
//  public ActorRef setupIndexMasterActor() 
//  {
//    ActorSystem system = applicationContext.getBean(ActorSystem.class);
//    SetupIndexService setupIndexService = (SetupIndexService) applicationContext.getBean(SetupIndexService.class);
//    SampleDataGeneratorService sampleDataGeneratorService = (SampleDataGeneratorService) applicationContext.getBean(SampleDataGeneratorService.class);
//    IndexProductDataService indexProductData = (IndexProductDataService) applicationContext.getBean(IndexProductDataService.class);
//    return system.actorOf(Props.create(SetupIndexMasterActor.class, 2, 100, 100, setupIndexService, sampleDataGeneratorService, indexProductData));
//  }
  
  @Bean(autowire=Autowire.BY_NAME, name="setupIndexMasterActor")
//  @Scope("prototype")
  @DependsOn(value={"actorSystem"})
  public ActorRef setupIndexMasterActor() 
  {
    ActorSystem system = applicationContext.getBean(ActorSystem.class);
    SetupIndexService setupIndexService = (SetupIndexService) applicationContext.getBean(SetupIndexService.class);
    SampleDataGeneratorService sampleDataGeneratorService = (SampleDataGeneratorService) applicationContext.getBean(SampleDataGeneratorService.class);
    IndexProductDataService indexProductData = (IndexProductDataService) applicationContext.getBean(IndexProductDataService.class);
    return system.actorOf(Props.create(SetupIndexMasterActor.class, 2, 100, 100, setupIndexService, sampleDataGeneratorService, indexProductData), "setupIndexMasterActor");
    
//    ActorRef master = system.actorOf(SpringExtProvider.get(system).props("setupIndexMasterActor"));
//    return master;
  }
  
//  @PreDestroy
//  private void destroyActorSystem()
//  {
//	  ActorSystem system = applicationContext.getBean(ActorSystem.class);
//	  system.shutdown();
//  }
}
