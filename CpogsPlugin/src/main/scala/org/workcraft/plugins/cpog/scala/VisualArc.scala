package org.workcraft.plugins.cpog.scala

import java.awt.geom.AffineTransform
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import org.workcraft.dom.visual.connections.StaticPolylineData
import org.workcraft.dom.visual.connections.StaticConnectionDataVisitor
import org.workcraft.dom.visual.connections.StaticBezierData
import org.workcraft.dom.visual.connections.StaticVisualConnectionData
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
import Expressions._
import Util._
import Scalaz._
import _root_.scala.collection.JavaConversions._


sealed trait VisualArc extends org.workcraft.dom.visual.connections.VisualConnectionData {
  def accept[T](visitor : ConnectionDataVisitor[T]) = this match {
    case b : VisualArc.Bezier => visitor.visitBezier(b)
    case p : VisualArc.Polyline => visitor.visitPolyline(p)
  }
}

object VisualArc {
  case class Bezier(cp1: ModifiableExpression[Point2D], cp2: ModifiableExpression[Point2D]) extends VisualArc with org.workcraft.dom.visual.connections.BezierData
  case class Polyline(cp: List[ModifiableExpression[Point2D]]) extends VisualArc with org.workcraft.dom.visual.connections.PolylineData {
    override def controlPoints = asJavaCollection(cp)
  }

  def makeBezierVisitor(p: (Point2D, Point2D)): StaticVisualConnectionData = new StaticVisualConnectionData {
    override def accept[T](visitor: StaticConnectionDataVisitor[T]): T = {
      val data = new StaticBezierData {
        override def cp1 = p._1
        override def cp2 = p._2
      }

      return visitor.visitBezier(data)
    }
  }
  
  def makePolylineVisitor(p: Iterable[Point2D]) : StaticVisualConnectionData = new StaticVisualConnectionData {
    override def accept[T](visitor: StaticConnectionDataVisitor[T]): T = {
      val data = new StaticPolylineData {
        override def controlPoints = new java.util.ArrayList[Point2D](p)
      }

      return visitor.visitPolyline(data)
    }
  }

  val properties = new VisualConnectionProperties {
    override def getDrawColor = Color.green
    override def getArrowWidth = 0.1
    override def getArrowLength = 0.2
    override def hasArrow = true
    override def getStroke = new BasicStroke(0.05f)
  }

  def gui(touchable: Component => Expression[Touchable])(arc : Arc): Expression[ConnectionGui] =
  {
    
    for (
      c1 <- touchable(arc.first);
      c2 <- touchable(arc.second);
      
      val context = new VisualConnectionContext {
        override def component1 = c1
        override def component2 = c2
      };

      visualArc : VisualArc <- arc.visual;
      gui <- visualArc match {
        case b: Bezier =>
          for (
            cp1 <- b.cp1;
            cp2 <- b.cp2
          ) yield makeBezierVisitor(cp1, cp2)
        case p: Polyline =>
          (for (
            cp <- Expressions.joinCollection(p.cp)
          ) yield makePolylineVisitor(cp) ) : Expression[StaticVisualConnectionData]
      }) yield VisualConnectionGui.getConnectionGui(properties, context, gui)
    }
   
  def image( gui : Expression[ConnectionGui] ) : Expression[BoundedColorisableGraphicalContent] = 
    for (gui <- gui) yield new BoundedColorisableGraphicalContent (gui.graphicalContent, gui.shape.getBoundingBox)
}
