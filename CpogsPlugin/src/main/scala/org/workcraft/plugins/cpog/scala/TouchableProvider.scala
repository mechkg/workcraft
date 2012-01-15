package org.workcraft.plugins.cpog.scala

import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

import org.workcraft.dom.visual.Touchable
import org.workcraft.dom.visual.TransformHelper
import org.workcraft.plugins.cpog.scala.nodes.Arc
import org.workcraft.plugins.cpog.scala.nodes.Component
import org.workcraft.plugins.cpog.scala.nodes.Node
import org.workcraft.plugins.cpog.scala.nodes.RhoClause
import org.workcraft.plugins.cpog.scala.nodes.Variable
import org.workcraft.plugins.cpog.scala.nodes.Vertex
import org.workcraft.scala.Expressions._
import org.workcraft.scala.Scalaz.maImplicit


object TouchableProvider {
  
  val vertexTouchable = new Touchable {
    val size = VisualVertex.size
    val bb = new Rectangle2D.Double(-size/2, -size/2, size, size)
    val center = new Point2D.Double(0,0)
    
    def hitTest (point: Point2D.Double) = point.distance(0,0) < size/2
    def getBoundingBox = bb
    def getCenter = center
  }
   
  def localTouchable (c : Component) : Expression[Touchable] =
    c match {
      case v : Vertex => 
        for ( image <- VisualVertex.image(v))
        yield image.touchable
      case v : Variable =>
        for ( image <- VisualVariable.image(v) ) 
        yield image.touchable
      case r : RhoClause =>
        for ( image <- VisualRhoClause.image(r) ) 
        yield image.touchable
    }
  
  def touchable (transform: Component => Expression[AffineTransform])(node : Node) : Expression[Touchable] = node match {
    case c : Component => for ( lt <- localTouchable(c); t <- transform(c) ) yield TransformHelper.transform(lt,t)
    case a : Arc => 
      for ( first <- touchable(transform)(a.first);
            second <- touchable(transform)(a.second);
            visual <- a.visual;
            gui <- VisualArc.gui(first, second, visual)) yield gui.shape
  }
}
