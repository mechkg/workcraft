package org.workcraft.plugins.cpog.scala
import org.workcraft.dom.visual.HitMan
import org.workcraft.dom.visual.Touchable
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.Expressions
import org.workcraft.dependencymanager.advanced.core.GlobalCache._
import nodes._
import Util._
import scala.collection.JavaConversions._

object HitTester {
  def hitTester[T](nodes: Expression[List[T]], touchable: T => Expression[Touchable]): org.workcraft.gui.graph.tools.HitTester[T] = {
    val transformedTouchableProvider = eval[T, Touchable](touchable)
    new HitMan.Flat[T](() => asJavaIterable(eval(nodes)), transformedTouchableProvider).getHitTester()
  }
}