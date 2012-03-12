package org.workcraft.dom.visual.connections

import java.awt.Color
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import org.workcraft.util.Geometry
import org.workcraft.graphics.ColorisableGraphicalContent
import org.workcraft.graphics.Touchable
import org.workcraft.scala.Expressions._
import org.workcraft.graphics.DrawRequest
import org.workcraft.graphics.Coloriser
import org.workcraft.dom.visual.DrawHelper
import org.workcraft.graphics.TouchableC
import org.workcraft.graphics.BoundingBox

object VisualConnectionGui {

  val HitThreshold = 0.2

  case class ExprConnectionGui(
    shape: Expression[Touchable], graphicalContent: Expression[ColorisableGraphicalContent], parametricCurve: Expression[ParametricCurve])

  def makeConnectionTouchable(curve: ParametricCurve, partial: PartialCurveInfo): TouchableC =
    new TouchableC(new Touchable {
      override def hitTest(point: Point2D.Double) = {
        val nearestT = curve.getNearestPointT(point)
        nearestT < partial.tEnd && nearestT > partial.tStart && (curve.getPointOnCurve(nearestT).distance(point) < HitThreshold)
      }
      override def boundingBox = BoundingBox(curve.getBoundingBox)
    }, curve.getPointOnCurve(0.5))

  def makeGraphicalContent(cInfo: PartialCurveInfo, connProps: VisualConnectionProperties, connectionShape: Shape) = {
    new ColorisableGraphicalContent {
      override def draw(r: DrawRequest) {
        val g = r.graphics

        val color = Coloriser.colorise(connProps.getDrawColor, r.colorisation.foreground)
        g.setColor(color)
        g.setStroke(connProps.getStroke)
        g.draw(connectionShape)

        if (connProps.hasArrow)
          DrawHelper.drawArrowHead(g, color, cInfo.arrowHeadPosition, cInfo.arrowOrientation,
            connProps.getArrowLength, connProps.getArrowWidth)
      }
    }
  }

  def getConnectionGui(properties: VisualConnectionProperties, context: VisualConnectionContext, data: StaticVisualConnectionData) = {
    val curve = data match {
      case Polyline(cps) => PolylineGui.makeCurve(properties, context, cps)
      case Bezier(cp1, cp2) => BezierGui.makeCurve(properties, context, cp1, cp2)
    }
    val curveInfo = Geometry.buildConnectionCurveInfo(properties, context.c1.forJava, context.c2.forJava, curve, 0)
    val visiblePath = curve.getShape(curveInfo.tStart, curveInfo.tEnd)
    val gc = makeGraphicalContent(curveInfo, properties, visiblePath)
    val touchable = makeConnectionTouchable(curve, curveInfo)

    new ConnectionGui(touchable, gc, curve)
  }
}
