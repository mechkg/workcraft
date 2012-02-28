package org.workcraft.pluginmanager

import org.scalatest.Spec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.util.UUID
import org.workcraft.logging.StandardStreamLogger

@RunWith(classOf[JUnitRunner])
class PluginManagerTest extends Spec {
  describe ("PluginManager") {
    val version = UUID.fromString("b9a4c2f9-d937-4abd-9e50-c9fdb156a28e")
    val manifestPath = ClassLoader.getSystemResource("manifest").getPath()
    val packages = List("org.workcraft.plugins")
    
    var logger_ = new StandardStreamLogger
    val logger = () => logger_ 
    
   it ("should work :)") {
      val manager = new PluginManager(version, packages, manifestPath, false)(logger)
    }
  }
}
