package org.workcraft.plugins.stg21
import org.workcraft.graphics.Touchable
import org.workcraft.plugins.stg21.types.VisualArc
import org.workcraft.dom.visual.connections.VisualConnectionGui
import org.workcraft.dom.visual.connections.VisualConnectionProperties
import java.awt.Color
import java.awt.BasicStroke
import org.workcraft.dom.visual.connections.StaticVisualConnectionData
import org.workcraft.dom.visual.connections.Polyline
import org.workcraft.dom.visual.connections.Bezier
import org.workcraft.graphics.RichGraphicalContent
import org.workcraft.graphics.TouchableC
import org.workcraft.graphics.BoundedColorisableGraphicalContent
import org.workcraft.dom.visual.connections.VisualConnectionContext

object VisualConnectionG {
  
  val properties = new VisualConnectionProperties {
    override def getDrawColor = Color.green
    override def getArrowWidth = 0.1
    override def getArrowLength = 0.2
    override def hasArrow = true
    override def getStroke = new BasicStroke(0.05f)
  }

  def getConnectionGui(first : TouchableC, second : TouchableC, arc : VisualArc) : RichGraphicalContent = {
    val gui = VisualConnectionGui.getConnectionGui(properties, VisualConnectionContext(first, second), arc)
    new RichGraphicalContent (new BoundedColorisableGraphicalContent(gui.graphicalContent, gui.shape.touchable.boundingBox), gui.shape)
  }
}