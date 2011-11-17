package org.workcraft.pluginmanager

import org.scalatest.Spec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PluginFinderTest extends Spec {
  describe("PluginFinder") {
    val classPathLocations = System.getProperty("java.class.path").split(System.getProperty("path.separator")).toList

    println("Class path location: " + classPathLocations)

    val result = new PluginFinder(List("org.workcraft.plugins")).searchClassPath()

    println(result)

    it("should have found the GoodPlugin A directly in a classpath folder") {
      assert(result.exists(_.equals("org.workcraft.plugins.GoodPluginA")))
    }
    it("should have found the GoodPlugin B in a subfolder of a classpath folder") {
      assert(result.exists(_.equals("org.workcraft.plugins.GoodPluginB")))
    }
    it("should have found the Bad class in a subfolder of a classpath folder") {
      assert(result.exists(_.equals("org.workcraft.plugins.Bad")))
    }
    it("should have found the AbstractPlugin") {
      assert(result.exists(_.equals("org.workcraft.plugins.AbstractPlugin")))
    }
    it("should have found the GoodPlugin C in a jar file on the classpath") {
      assert(result.exists(_.equals("org.workcraft.plugins.GoodPluginC")))
    }
    it("should have found the NotAPlugin in a jar file on the classpath") {
      assert(result.exists(_.equals("org.workcraft.plugins.NotAPlugin")))
    }

    it("should have ignored the PluginInAWrongPackage") {
      assert(!result.exists(_.equals("somepackage.PluginInAWrongPackage")))
    }
  }
}