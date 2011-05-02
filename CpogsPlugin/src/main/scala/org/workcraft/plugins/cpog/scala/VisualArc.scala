package org.workcraft.plugins.cpog.scala

import org.workcraft.dependencymanager.advanced.core.Expressions
import org.workcraft.dom.visual.connections.BezierData
import org.workcraft.dom.visual.connections.ConnectionDataVisitor
import org.workcraft.dom.visual.connections.VisualConnectionData
import org.workcraft.dom.visual.connections.VisualConnectionContext
import org.workcraft.dom.visual.connections.VisualConnectionGui
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import java.awt.geom.Point2D
import java.awt.BasicStroke
import java.awt.Color
import org.workcraft.dom.visual.connections.VisualConnectionProperties
import nodes._
import org.workcraft.dom.visual.Touchable
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dom.visual.connections.ConnectionGui
import Util.monadicSyntax
import _root_.scala.collection.JavaConversions._


sealed trait VisualArc

object VisualArc {
  case class Bezier(cp1: ModifiableExpression[Point2D], cp2: ModifiableExpression[Point2D]) extends VisualArc
  case class Polyline(cp: List[ModifiableExpression[Point2D]]) extends VisualArc

  implicit def qwe(p: (Point2D, Point2D)): VisualConnectionData = new VisualConnectionData {
    override def accept[T](visitor: ConnectionDataVisitor[T]): T = {
      val data = new BezierData {
        override def cp1 = p._1
        override def cp2 = p._2
      }

      return visitor.visitBezier(data)
    }
  }
  
  implicit def xru(p: Iterable[Point2D]) : VisualConnectionData = null

  val properties = new VisualConnectionProperties {
    override def getDrawColor = Color.green
    override def getArrowWidth = 0.1
    override def getArrowLength = 0.2
    override def hasArrow = true
    override def getStroke = new BasicStroke(0.05f)
  }

  def gui(arc: Arc, transformedComponents: Component => Expression[Touchable]): Expression[ConnectionGui] =
  {
    
    for (
      c1 <- transformedComponents(arc.first);
      c2 <- transformedComponents(arc.second);
      
      val context = new VisualConnectionContext {
        override def component1 = c1
        override def component2 = c2
      };

      visualArc : VisualArc <- arc.visual;
      gui:VisualConnectionData <- visualArc match {
        case b: Bezier => null:Expression[VisualConnectionData]
        /*  for (
            cp1 <- b.cp1;
            cp2 <- b.cp2
          ) yield qwe(cp1, cp2)*/
        case p: Polyline => null
          /*for (
            cp <- Expressions.joinCollection()(asJavaList(p.cp))
          ) yield xru(cp)*/
      }) yield VisualConnectionGui.getConnectionGui(properties, context, gui)
      


    }
}