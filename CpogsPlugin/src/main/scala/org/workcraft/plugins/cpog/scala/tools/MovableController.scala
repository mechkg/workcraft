package org.workcraft.plugins.cpog.scala.tools

import org.workcraft.dom.visual.Touchable
import java.awt.Point
import org.workcraft.util.Maybe
import org.workcraft.scala.Expressions._
import org.workcraft.util.Maybe.Util._
import java.awt.geom.Point2D

import org.workcraft.plugins.cpog.scala.nodes._

object MovableController {
  def position(component: Component): ModifiableExpression[Point2D.Double] = component.visualProperties.position

  def position(node: Node): Maybe[ModifiableExpression[Point2D.Double]] = node match {
    case _: Arc => nothing()
    case c: Component => just(position(c))
  }

  def positionWithDefaultZero(node: Node): Expression[Point2D.Double] = node match {
    case _: Arc => constant(new Point2D.Double(0, 0))
    case c: Component => position(c)
  }
}
