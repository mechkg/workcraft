package org.workcraft.scala.grapheditor.tools
import org.workcraft.dom.visual.HitMan
import org.workcraft.dom.visual.Touchable
import org.workcraft.dependencymanager.advanced.core.{Expression => JExpression}
import org.workcraft.dependencymanager.advanced.core.GlobalCache._
import org.workcraft.scala.Util._
import org.workcraft.scala.Expressions.Expression
import scala.collection.JavaConversions._

object HitTester {
  def create[T](nodes: Expression[_ <: Iterable[T]], touchable: T => Expression[Touchable]): org.workcraft.gui.graph.tools.HitTester[T] = {
    val transformedTouchableProvider = eval[T, Touchable](((_ : Expression[Touchable]).jexpr)compose touchable)
    new HitMan.Flat[T](() => asJavaIterable(eval(nodes.jexpr)), transformedTouchableProvider).getHitTester
  }
}
