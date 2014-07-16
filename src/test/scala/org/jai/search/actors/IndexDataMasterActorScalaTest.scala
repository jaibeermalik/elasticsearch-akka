package org.jai.search.actors

import org.junit.Before
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit

import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.TestActorRef

class IndexDataMasterActorScalaTest extends AssertionsForJUnit {

  var actorSystem: ActorSystem = ActorSystem.create("TestSearchIndexingSystemScala", ConfigFactory.load().getConfig("TestSearchIndexingSystemScala"));
  val actorRef = TestActorRef.create(actorSystem, Props.create(classOf[SetupIndexMasterActorScala], null, null, null));

  @Before def initialize() {

  }

  @Test
  def receive() {
    actorRef.receive("hi")
  }

}