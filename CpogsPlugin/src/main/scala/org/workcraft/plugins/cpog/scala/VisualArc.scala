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
import org.workcraft.scala.Expressions._
import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import _root_.scala.collection.JavaConversions._
import org.workcraft.plugins.cpog.scala.nodes.snapshot.SnapshotMaker
import org.workcraft.dom.visual.connections.RelativePoint


sealed trait VisualArc extends org.workcraft.dom.visual.connections.VisualConnectionData {
  def accept[T](visitor : ConnectionDataVisitor[T]) = this match {
    case b : VisualArc.Bezier => visitor.visitBezier(b)
    case p : VisualArc.Polyline => visitor.visitPolyline(p)
  }
}

object VisualArc {
  case class Bezier(cp1: ModifiableExpression[RelativePoint], cp2: ModifiableExpression[RelativePoint]) extends VisualArc with org.workcraft.dom.visual.connections.BezierData
  case class Polyline(cp: List[ModifiableExpression[Point2D]]) extends VisualArc with org.workcraft.dom.visual.connections.PolylineData {
    override def controlPoints = asJavaCollection(cp)
  }

  
  def makePolylineVisitable(p: Iterable[Point2D]) : StaticVisualConnectionData = new StaticVisualConnectionData {
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

  def gui(first: Touchable, second: Touchable, visualArc : VisualArc): Expression[ConnectionGui] =
    {
        val context = new VisualConnectionContext {
          override def component1 = first
          override def component2 = second
        }

        for (gui <- SnapshotMaker.makeVisualArcSnapshot(visualArc): Expression[_ <: StaticVisualConnectionData])
        yield VisualConnectionGui.getConnectionGui(properties, context, gui)
    }
  
  def image( gui : Expression[ConnectionGui] ) : Expression[BoundedColorisableGraphicalContent] = 
    for (gui <- gui) yield new BoundedColorisableGraphicalContent (gui.graphicalContent, gui.shape.getBoundingBox)
}
