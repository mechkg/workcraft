package org.workcraft.dom.visual.connections

import java.awt.Color
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import org.workcraft.graphics.ColorisableGraphicalContent
import org.workcraft.graphics.Touchable
import org.workcraft.scala.Expressions._
import org.workcraft.graphics.DrawRequest
import org.workcraft.graphics.Coloriser
import org.workcraft.graphics.TouchableC
import org.workcraft.graphics.BoundingBox
import org.workcraft.graphics.PartialCurveInfo
import org.workcraft.graphics.VisualCurveProperties
import java.awt.geom.Path2D
import java.awt.geom.AffineTransform
import java.awt.BasicStroke
import org.workcraft.graphics.Geometry.buildConnectionCurveInfo
import org.workcraft.dom.visual.connections.VisualConnectionContext
import org.workcraft.dom.visual.connections.StaticVisualConnectionData
import org.workcraft.dom.visual.connections.Polyline
import org.workcraft.dom.visual.connections.Bezier
import org.workcraft.dom.visual.connections.ConnectionGui
import org.workcraft.dom.visual.connections.BezierGui

object VisualConnectionGui {

  val HitThreshold = 0.2
  
	def drawArrowHead(g: Graphics2D, color: Color, headPosition: Point2D, orientation: Double, length: Double, width: Double) = {
		val arrowShape = new Path2D.Double()
		arrowShape.moveTo(-length, -width / 2)
		arrowShape.lineTo(-length, width / 2)
		arrowShape.lineTo(0,0)
		arrowShape.closePath()

		val arrowBounds = arrowShape.getBounds2D()
		arrowBounds.setRect(arrowBounds.getMinX()+0.05f, arrowBounds.getMinY(), arrowBounds.getWidth(), arrowBounds.getHeight())

		val arrowTransform = new AffineTransform()
		arrowTransform.translate(headPosition.getX(), headPosition.getY())
		arrowTransform.rotate(orientation)

		val transformedArrowShape = arrowTransform.createTransformedShape(arrowShape)

		g.setColor(color)
		g.setStroke(new BasicStroke(width.toFloat))
		g.fill(transformedArrowShape)		
	}  

  case class ExprConnectionGui(
    shape: Expression[Touchable], graphicalContent: Expression[ColorisableGraphicalContent], parametricCurve: Expression[org.workcraft.graphics.ParametricCurve])

  def makeConnectionTouchable(curve: org.workcraft.graphics.ParametricCurve, partial: PartialCurveInfo): TouchableC =
    new TouchableC(new Touchable {
      override def hitTest(point: Point2D.Double) = {
        val nearestT = curve.nearestPointT(point)
        nearestT < partial.tEnd && nearestT > partial.tStart && (curve.pointOnCurve(nearestT).distance(point) < HitThreshold)
      }
      override def boundingBox = BoundingBox(curve.boundingBox)
    }, curve.pointOnCurve(0.5))

  def makeGraphicalContent(cInfo: PartialCurveInfo, connProps: VisualCurveProperties, connectionShape: Shape) = {
    new ColorisableGraphicalContent {
      override def draw(r: DrawRequest) {
        val g = r.graphics

        val color = Coloriser.colorise(connProps.color, r.colorisation.foreground)
        g.setColor(color)
        g.setStroke(connProps.stroke)
        g.draw(connectionShape)

        connProps.arrow.foreach ( arrow =>
          drawArrowHead(g, color, cInfo.arrowHeadPosition, cInfo.arrowOrientation,
            arrow.length, arrow.width)
            )
      }
    }
  }

  def getConnectionGui(properties: VisualCurveProperties, context: VisualConnectionContext, data: StaticVisualConnectionData) = {
    val curve = data match {
      case Polyline(cps) => PolylineGui.makeCurve(properties, context, cps)
      case Bezier(cp1, cp2) => BezierGui.makeCurve(context, cp1, cp2)
    }
    val curveInfo = buildConnectionCurveInfo(properties.arrow, context.c1.touchable, context.c2.touchable, curve, 0)
    val visiblePath = curve.shape(curveInfo.tStart, curveInfo.tEnd)
    val gc = makeGraphicalContent(curveInfo, properties, visiblePath)
    val touchable = makeConnectionTouchable(curve, curveInfo)

    new ConnectionGui(touchable, gc, curve)
  }
}
