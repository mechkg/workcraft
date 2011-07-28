package org.workcraft.plugins.cpog.scala.tools
import org.workcraft.dom.visual.HitMan
import org.workcraft.dom.visual.Touchable
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.GlobalCache._
import org.workcraft.plugins.cpog.scala.nodes._
import org.workcraft.scala.Util._
import scala.collection.JavaConversions._

object HitTester {
  def create[T](nodes: Expression[_ <: Iterable[T]], touchable: T => Expression[Touchable]): org.workcraft.gui.graph.tools.HitTester[T] = {
    val transformedTouchableProvider = eval[T, Touchable](touchable)
    new HitMan.Flat[T](() => asJavaIterable(eval(nodes)), transformedTouchableProvider).getHitTester
  }
}
