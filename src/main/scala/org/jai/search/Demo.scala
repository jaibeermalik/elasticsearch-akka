package org.jai.search

import org.jai.search.client.SearchClientService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.Assert

@Service
class Demo {

  @Autowired
  var searchClientService: SearchClientService = null

  def main(args: Array[String]) {
    println("Hello, world!")
  }
  
  def checkClientService()
  {
    println("Checking client service...")
     Assert.notNull(searchClientService,  "Spring service should not be null")
      println(searchClientService.getClass().getName())
  }
  
  def setSearchClientService(newSearchClientService: SearchClientService)
  {
    searchClientService = newSearchClientService
  }
}