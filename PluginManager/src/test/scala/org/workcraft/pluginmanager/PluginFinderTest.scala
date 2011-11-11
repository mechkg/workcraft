import org.scalatest.Spec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.workcraft.pluginmanager.PluginFinder

@RunWith(classOf[JUnitRunner])
class PluginFinderTest extends Spec {
  describe("PluginFinder") {
   
    it("should pop values in last-in-first-out order") {
      PluginFinder.searchClassPath(List("org.workcraft")) map println  
    }

    it("should throw NoSuchElementException if an empty stack is popped") (pending)
  }
}