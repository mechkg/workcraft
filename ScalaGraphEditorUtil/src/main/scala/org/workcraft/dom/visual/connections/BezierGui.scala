package org.workcraft.dom.visual.connections

import java.awt.Shape
import java.awt.geom.CubicCurve2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import org.workcraft.graphics.Java2DDecoration._
import org.workcraft.graphics.VisualCurveProperties
import org.workcraft.graphics.Geometry.complexMultiply
import org.workcraft.graphics.Geometry.complexInverse
import org.workcraft.graphics.Java2DDecoration._
import org.workcraft.graphics.ParametricCurve

/**
 * A connection control point coordinate, in a coordinate system where the first connected component is at (0, 0) and the second one is at (1, 0)
 */
case class RelativePoint(point: Point2D.Double) {
  def toUserSpace(p1: Point2D.Double, p2: Point2D.Double) = complexMultiply(point, p2 - p1) + p1
}

object RelativePoint {
  val oneThird = new RelativePoint(new Point2D.Double(1.0 / 3.0, 0.0))
  val twoThirds = new RelativePoint(new Point2D.Double(2.0 / 3.0, 0.0))

  def fromUserSpace(p1: Point2D.Double, p2: Point2D.Double, p: Point2D.Double) = complexInverse(p2 - p1).map(complexMultiply(p - p1, _))
}

object BezierGui {

  def makeCurve(context: VisualConnectionContext, cp1: RelativePoint, cp2: RelativePoint): ParametricCurve = {
    val curve2D = new CubicCurve2D.Double
    val c1 = context.c1.center
    val c2 = context.c2.center
    val absoluteCp1 = cp1.toUserSpace(c1, c2)
    val absoluteCp2 = cp2.toUserSpace(c1, c2)
    curve2D.setCurve(c1, absoluteCp1, absoluteCp2, c2)
    new Curve(curve2D)
  }

  private class Curve(fullCurve2D: CubicCurve2D.Double) extends ParametricCurve {

    override def shape(tStart: Double, tEnd: Double): Shape = {
      val firstSplit = fullCurve2D.split(tStart)
      val secondSplit = firstSplit._2.split((tEnd - tStart) / (1 - tStart))
      secondSplit._1
    }

    override def nearestPointT(pt: Point2D.Double): Double = {
      // FIXME: should be done using some proper algorithm
      // DONT FIXME: it ain't broken :)
      var nearest = 0.0
      var nearestDist = java.lang.Double.MAX_VALUE

      Range.Double(0.01, 1.0, 0.01).foreach(t => {
        val samplePoint = fullCurve2D.getPointOnCurve(t)
        val distance = pt.distance(samplePoint)
        if (distance < nearestDist) {
          nearestDist = distance
          nearest = t
        }
      })

      nearest
    }

    override def pointOnCurve(t: Double): Point2D.Double = fullCurve2D.getPointOnCurve(t)

    override def derivativeAt(t: Double): Point2D.Double = fullCurve2D.getDerivative(t)

    override def secondDerivativeAt(t: Double): Point2D.Double = fullCurve2D.getSecondDerivative(t)

    override def boundingBox: Rectangle2D.Double = {
      val boundingBox = fullCurve2D.bounds
      boundingBox.add(boundingBox.getMinX - VisualConnectionGui.HitThreshold, boundingBox.getMinY - VisualConnectionGui.HitThreshold)
      boundingBox.add(boundingBox.getMinX - VisualConnectionGui.HitThreshold, boundingBox.getMaxY + VisualConnectionGui.HitThreshold)
      boundingBox.add(boundingBox.getMaxX + VisualConnectionGui.HitThreshold, boundingBox.getMinY - VisualConnectionGui.HitThreshold)
      boundingBox.add(boundingBox.getMaxX + VisualConnectionGui.HitThreshold, boundingBox.getMaxY + VisualConnectionGui.HitThreshold)
      boundingBox
    }
  }
}
