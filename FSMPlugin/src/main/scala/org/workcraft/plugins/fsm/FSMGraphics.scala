package org.workcraft.plugins.fsm
import org.workcraft.gui.CommonVisualSettings
import org.workcraft.scala.Expressions._
import scalaz._
import Scalaz._
import org.workcraft.graphics.Graphics._
import java.awt.BasicStroke
import org.workcraft.graphics.Graphics
import org.workcraft.graphics.LabelPositioning
import org.workcraft.graphics.BoundedColorisableGraphicalContent
import org.workcraft.gui.CommonVisualSettings
import org.workcraft.graphics.Touchable
import java.awt.geom.Rectangle2D
import java.awt.Color
import org.workcraft.graphics.VisualCurveProperties
import org.workcraft.dom.visual.connections.ConnectionGui
import java.awt.geom.Path2D
import org.workcraft.dom.visual.connections.VisualConnectionGui
import java.awt.geom.Point2D

object FSMGraphics {
  def stateImage(label: String, isInitial: Boolean, isTerminal: Boolean, settings: CommonVisualSettings): BoundedColorisableGraphicalContent = {
    
    val size = settings.size * 1.5
    val strokeWidth = settings.strokeWidth.toFloat / 2.0f
    val stroke = Some(new BasicStroke(strokeWidth), settings.foregroundColor)
    val fill = Some(settings.fillColor)

    val image = circle(size, stroke, fill).boundedColorisableGraphicalContent

    val labelImage =
      Graphics.label(label, settings.effectiveLabelFont, settings.foregroundColor).boundedColorisableGraphicalContent

    lazy val terminal =
      circle(size - settings.strokeWidth * 2, stroke, fill).boundedColorisableGraphicalContent

    lazy val initial = {
      val p = new Path2D.Double()
      p.moveTo(0, -size * 1.5)
      p.lineTo(0, -size/2 - 0.4)

      val arrow = VisualConnectionGui.arrowHead(settings.foregroundColor, new Point2D.Double(0, -size/2 - strokeWidth / 2), 0.5 * scala.math.Pi, 0.6, 0.4)

      path(p, new BasicStroke(strokeWidth * 1.5f), settings.foregroundColor).boundedColorisableGraphicalContent.compose(arrow)
    }

    val i1 = if (isTerminal) image.compose(terminal) else image
    val i2 = if (isInitial) i1.compose(initial) else i1

    
    i2.compose(
        if (labelImage.bounds.visual.width > size * 0.8)
          labelImage.alignSideways(i1, LabelPositioning.Bottom)
        else
        labelImage.align(i1, HorizontalAlignment.Center, VerticalAlignment.Center)
        )
  }

  val stateTouchable = CommonVisualSettings.settings.map(settings => Touchable.fromCircle(settings.size * 0.75))
}
