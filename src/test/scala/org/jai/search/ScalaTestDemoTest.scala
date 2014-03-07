package org.jai.search

import org.jai.search.Demo
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

class ScalaTestDemoTest extends UnitSpec {
  // tests go here...
    
    var demovar : Demo = null
    
    def loadContext(args: Array[String]) {

     val context:ApplicationContext = new ClassPathXmlApplicationContext("applicationContext-elasticsearch.xml")
     val myBean:Demo = context.getBean("demo").asInstanceOf[Demo]
     print(myBean)
     print(myBean.searchClientService)
     demovar = myBean
    }
    
    "A demo" should " print " in
    {
        loadContext(null)
        demovar.checkClientService
    }
}