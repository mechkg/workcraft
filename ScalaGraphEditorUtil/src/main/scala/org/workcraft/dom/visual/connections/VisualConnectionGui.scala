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
import org.workcraft.graphics.BoundedColorisableGraphicalContent
import org.workcraft.graphics.GraphicalContent
import org.workcraft.graphics.Java2DDecoration._
import org.workcraft.graphics.Colorisation

object VisualConnectionGui {

  val HitThreshold = 0.2

  def drawArrowHead(g: Graphics2D, color: Color, headPosition: Point2D, orientation: Double, length: Double, width: Double) = {
    val arrowShape = new Path2D.Double()
    arrowShape.moveTo(-length, -width / 2)
    arrowShape.lineTo(-length, width / 2)
    arrowShape.lineTo(0, 0)
    arrowShape.closePath()

    val arrowTransform = new AffineTransform()
    arrowTransform.translate(headPosition.getX(), headPosition.getY())
    arrowTransform.rotate(orientation)

    val transformedArrowShape = arrowTransform.createTransformedShape(arrowShape)

    g.setColor(color)
    g.setStroke(new BasicStroke(width.toFloat))
    g.fill(transformedArrowShape)
  }

  def arrowHead(color: Color, headPosition: Point2D, orientation: Double, length: Double, width: Double) = {
    val arrowShape = new Path2D.Double()
    arrowShape.moveTo(-length, -width / 2)
    arrowShape.lineTo(-length, width / 2)
    arrowShape.lineTo(0, 0)
    arrowShape.closePath()

    val arrowTransform = new AffineTransform()
    arrowTransform.translate(headPosition.getX(), headPosition.getY())
    arrowTransform.rotate(orientation)

    val transformedArrowShape = arrowTransform.createTransformedShape(arrowShape)

    BoundedColorisableGraphicalContent(
      ColorisableGraphicalContent(colorisation => GraphicalContent(g => { g.setColor(Coloriser.colorise(color, colorisation.foreground)); g.setStroke(new BasicStroke(width.toFloat)); g.fill(transformedArrowShape) })),
      BoundingBox(transformedArrowShape.getBounds2D()))
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

  def makeGraphicalContent(cInfo: PartialCurveInfo, curve: org.workcraft.graphics.ParametricCurve, connProps: VisualCurveProperties, connectionShape: Shape) = {
    new ColorisableGraphicalContent {
      override def draw(r: DrawRequest) {
        val g = r.graphics

        val color = Coloriser.colorise(connProps.color, r.colorisation.foreground)
        g.setColor(color)
        g.setStroke(connProps.stroke)
        g.draw(connectionShape)

        connProps.arrow.foreach(arrow =>
          drawArrowHead(g, color, cInfo.arrowHeadPosition, cInfo.arrowOrientation,
            arrow.length, arrow.width))

	connProps.label.foreach ( label => {
	  val p = curve.pointOnCurve(0.5)
	  val d = curve.derivativeAt(0.5)
	  val dd = curve.secondDerivativeAt(0.5)

	  val q = if (d.getX < 0)  (d * -1)  else d

	  val labelPosition = new Point2D.Double(label.bounds.logical.getCenterX, 
						 if ((q cross dd) > -0.01) label.bounds.logical.getMaxY
						 else label.bounds.logical.getMinY)

	  val offset = p - labelPosition

	  val transform = AffineTransform.getTranslateInstance(offset.getX, offset.getY)
	  transform.concatenate(AffineTransform.getRotateInstance(q.getX, q.getY, labelPosition.getX, labelPosition.getY))

	  label.transform(transform).cgc.draw(r)
	})
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
    val gc = makeGraphicalContent(curveInfo, curve, properties, visiblePath)
    val touchable = makeConnectionTouchable(curve, curveInfo)

    new ConnectionGui(touchable, gc, curve)
  }
}
