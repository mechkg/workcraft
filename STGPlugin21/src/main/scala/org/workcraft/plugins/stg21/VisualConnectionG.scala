package org.workcraft.plugins.stg21
import org.workcraft.dom.visual.Touchable
import org.workcraft.plugins.stg21.types.VisualArc
import org.workcraft.dom.visual.connections.VisualConnectionGui
import org.workcraft.dom.visual.connections.VisualConnectionProperties
import java.awt.Color
import java.awt.BasicStroke
import org.workcraft.dom.visual.connections.VisualConnectionContext
import org.workcraft.dom.visual.connections.StaticVisualConnectionData
import org.workcraft.dom.visual.connections.StaticConnectionDataVisitor
import org.workcraft.dom.visual.connections.StaticPolylineData
import org.workcraft.plugins.stg21.types.Bezier
import org.workcraft.plugins.stg21.types.Polyline
import org.workcraft.dom.visual.connections.StaticBezierData
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent

object VisualConnectionG {
      
  
  implicit def asStaticVisualConnectionData(arc : VisualArc) : StaticVisualConnectionData = new StaticVisualConnectionData{
    override def accept[R](visitor : StaticConnectionDataVisitor[R]) : R = {
      arc match {
        case Bezier(cp1_, cp2_) => visitor.visitBezier(new StaticBezierData{
          val cp1 = cp1_
          val cp2 = cp2_
        })
        case Polyline(cps) => visitor.visitPolyline(new StaticPolylineData{
          val controlPoints = scala.collection.JavaConversions.asJavaList(cps)
        })
      }
    }
  }
  
  val properties = new VisualConnectionProperties {
    override def getDrawColor = Color.green
    override def getArrowWidth = 0.1
    override def getArrowLength = 0.2
    override def hasArrow = true
    override def getStroke = new BasicStroke(0.05f)
  }

  def getConnectionGui(first : Touchable, second : Touchable, arc : VisualArc) : RichGraphicalContent = {
    val gui = VisualConnectionGui.getConnectionGui(properties, new VisualConnectionContext{
      override def component1 = first
      override def component2 = second
    }, arc)
    new RichGraphicalContent (gui.graphicalContent, gui.shape.getBoundingBox, gui.shape)
  }
}