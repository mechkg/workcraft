package org.workcraft.plugins.stg21
import org.workcraft.graphics.Touchable
import org.workcraft.plugins.stg21.types.VisualArc
import org.workcraft.dom.visual.connections.VisualConnectionGui
import java.awt.Color
import java.awt.BasicStroke
import org.workcraft.dom.visual.connections.StaticVisualConnectionData
import org.workcraft.dom.visual.connections.Polyline
import org.workcraft.dom.visual.connections.Bezier
import org.workcraft.graphics.stg.RichGraphicalContent
import org.workcraft.graphics.TouchableC
import org.workcraft.graphics.BoundedColorisableGraphicalContent
import org.workcraft.dom.visual.connections.VisualConnectionContext
import org.workcraft.graphics.stg.NotSoRichGraphicalContent
import org.workcraft.graphics.VisualCurveProperties
import org.workcraft.graphics.Arrow

object VisualConnectionG {
  val properties = VisualCurveProperties(Color.green, Some(Arrow(0.1, 0.2)), new BasicStroke(0.05f))

  def getConnectionGui(first : TouchableC, second : TouchableC, arc : VisualArc) : RichGraphicalContent = {
    val gui = VisualConnectionGui.getConnectionGui(properties, VisualConnectionContext(first, second), arc)
    s => new NotSoRichGraphicalContent (new BoundedColorisableGraphicalContent(gui.graphicalContent, gui.shape.touchable.boundingBox), gui.shape)
  }
}
