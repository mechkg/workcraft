package org.workcraft.scala.grapheditor.tools
import org.workcraft.dom.visual.HitMan
import org.workcraft.dependencymanager.advanced.core.{Expression => JExpression}
import org.workcraft.dependencymanager.advanced.core.GlobalCache._
import org.workcraft.scala.Util._
import org.workcraft.scala.Expressions.Expression
import scala.collection.JavaConversions._
import java.awt.geom.Point2D
import org.workcraft.util.Maybe
import pcollections.TreePVector
import pcollections.PCollection
import org.workcraft.graphics.Touchable

trait HitTester[N] {
  def hitTest(point : Point2D.Double) : Option[N]
  def boxHitTest(boxStart : Point2D.Double, boxEnd : Point2D.Double) : List[N]
  def forJava : org.workcraft.gui.graph.tools.HitTester[N] = {
    val self = this
    new org.workcraft.gui.graph.tools.HitTester[N] {
      def hitTest(point : Point2D.Double) : Maybe[N] = self.hitTest(point) match {
        case None => org.workcraft.util.Maybe.Util.nothing[N]
        case Some(x) => org.workcraft.util.Maybe.Util.just(x)
      } 
      def boxHitTest(boxStart : Point2D.Double, boxEnd : Point2D.Double) : PCollection[N] = TreePVector.from(self.boxHitTest(boxStart, boxEnd))
    }
  }
}

object HitTester {
  def create[T](nodes: Expression[_ <: Iterable[T]], touchable: T => Expression[Touchable]): HitTester[T] = {
    val transformedTouchableProvider = eval[T, Touchable](((_ : Expression[Touchable]).jexpr)compose touchable)
    new HitMan.Flat[T](eval(nodes.jexpr).toList, transformedTouchableProvider(_)).getHitTester
  }
}
