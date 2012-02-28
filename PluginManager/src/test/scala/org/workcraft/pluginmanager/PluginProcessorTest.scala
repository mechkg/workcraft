package org.workcraft.pluginmanager

import org.scalatest.Spec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.util.UUID

@RunWith(classOf[JUnitRunner])
class PluginProcessorTest extends Spec {
  describe("PluginProcessor") {
    
        val classPathLocations = System.getProperty("java.class.path").split(System.getProperty("path.separator")).toList

    println("Class path location: " + classPathLocations)


    val manifest = List("org.workcraft.plugins.GoodPluginA",
      "org.workcraft.plugins.Bad",
      "org.workcraft.plugins.PluginWithNoDefaultConstructor",
      "org.workcraft.plugins.AbstractPlugin")

    val results = PluginProcessor.processClasses(manifest)

    it("should report plugins that have no default constructor") {
      assert(results.errors.find(x => x match {
        case PluginError.NoDefaultConstructor(name) => name.equals("org.workcraft.plugins.PluginWithNoDefaultConstructor")
        case _ => false
      }).isDefined)
    }

    it("should report plugins that are abstract") {
      assert(results.errors.find(x => x match {
        case PluginError.Abstract(name) => name.equals("org.workcraft.plugins.AbstractPlugin")
        case _ => false
      }).isDefined)
    }
    
    it("should report plugins that could not be loaded") {
      assert(results.errors.find(x => x match {
        case PluginError.Exception(name, e) => name.equals("org.workcraft.plugins.Bad")
        case _ => false
      }).isDefined)
    }
    
    it("should load well-formed plugin classes") {
      assert(results.plugins.find(x => x.getName().equals("org.workcraft.plugins.GoodPluginA")).isDefined)
    }
  }
}