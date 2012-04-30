package org.workcraft.graphics

import java.awt.geom.AffineTransform
import java.awt.geom.CubicCurve2D
import java.awt.geom.NoninvertibleTransformException
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import Java2DDecoration._
import java.awt.Color
import java.awt.Stroke

trait ParametricCurve {
  def pointOnCurve(t: Double): Point2D.Double
  def nearestPointT(pt: Point2D.Double): Double
  def boundingBox: Rectangle2D.Double

  // TODO: subdivide the parametric curve automatically, removing the need for manual Shape creation
  def shape(tStart: Double, tEnd: Double): java.awt.Shape

  def derivativeAt(t: Double): Point2D.Double
  def secondDerivativeAt(t: Double): Point2D.Double
}

case class Arrow(width: Double, length: Double)

case class VisualCurveProperties(color: Color, arrow: Option[Arrow], stroke: Stroke, label: Option[BoundedColorisableGraphicalContent])

case class PartialCurveInfo(tStart: Double, tEnd: Double, arrowHeadPosition: Point2D.Double, arrowOrientation: Double)

object Geometry {
  def lerp(p1: Point2D.Double, p2: Point2D.Double, t: Double): Point2D.Double =
    new Point2D.Double(p1.getX() * (1 - t) + p2.getX() * t, p1.getY() * (1 - t) + p2.getY() * t)

  def createRectangle(p1: Point2D, p2: Point2D): Rectangle2D.Double = {
    val rect: Rectangle2D.Double = new Rectangle2D.Double(p1.getX(), p1.getY(), 0, 0)
    rect.add(p2)
    return rect
  }

  def createCubicCurve(p1: Point2D.Double, cp1: Point2D.Double, cp2: Point2D.Double, p2: Point2D.Double) =
    new CubicCurve2D.Double(p1.getX(), p1.getY(), cp1.getX(), cp1.getY(), cp2.getX(), cp2.getY(), p2.getX(), p2.getY())

  def optimisticInverse(transform: AffineTransform): AffineTransform = {
    try {
      return transform.createInverse()
    } catch {
      case ex: NoninvertibleTransformException =>
        throw new RuntimeException("Matrix inverse failed! Pessimists win :( ")

    }
  }

  def getBorderPointParameter(collisionNode: Touchable, curve: ParametricCurve, tStart: Double, tEnd: Double): Double = {
    var point: Point2D.Double = new Point2D.Double()
    var tstart = tStart
    var tend = tEnd

    while (Math.abs(tend - tstart) > 1e-6) {
      var t: Double = (tstart + tend) * 0.5
      point = curve.pointOnCurve(t)
      if (collisionNode.hitTest(point))
        tstart = t
      else
        tend = t
    }
    return tstart
  }

  def buildConnectionCurveInfo(arrow: Option[Arrow], t1: Touchable, t2: Touchable, curve: ParametricCurve, endCutoff: Double): PartialCurveInfo = {

    var tstart = getBorderPointParameter(t1, curve, 0, 1)
    var tend = getBorderPointParameter(t2, curve, 1, endCutoff)
    var arrowPos = curve.pointOnCurve(tend)
    var arrowOrientation = 0.0

    var dt = tend
    var t = 0.0
    var pt = new Point2D.Double

    arrow.foreach {
      case Arrow(width, length) => {
        val arrowLengthSq = length * length

        while (dt > 1e-6) {
          dt /= 2.0
          t += dt
          pt = curve.pointOnCurve(t)
          if (arrowPos.distanceSq(pt) < arrowLengthSq)
            t -= dt
        }

        tend = t
        arrowOrientation = scala.math.atan2(arrowPos.getY - pt.getY, arrowPos.getX - pt.getX)
      }
    }

    PartialCurveInfo(tstart, tend, arrowPos, arrowOrientation)
  }
  
  /**
   * Interpretes points as complex numbers and multiplies them.
   * Can be used for the scale-with-rotate (translates 'a' from the basis of (b, rotate90CCW(b)) to the basis of ((1, 0), (0, 1)))
   */
  def complexMultiply(a: Point2D.Double, b: Point2D.Double): Point2D.Double = {
    return new Point2D.Double(a.getX() * b.getX() - a.getY() * b.getY(), a.getX() * b.getY() + a.getY() * b.getX())
  }

  def complexInverse(a: Point2D.Double): Option[Point2D.Double] = {
    var sq: Double = a.distanceSq(0, 0)
    if (sq < 0.0000001)
      return None
    else
      return Some(new Point2D.Double(a.getX() / sq, -a.getY() / sq))
  }

  def changeBasis(p: Point2D, vx: Point2D, vy: Point2D): Point2D = {
    if ((vx dot vy) > 0.0000001)
      throw new RuntimeException("Vectors vx and vy must be orthogonal")

    var vysq = vy.distanceSq(0, 0)
    var vxsq = vx.distanceSq(0, 0)
    
    if (vysq < 0.0000001 || vxsq < 0.0000001)
      throw new RuntimeException("Vectors vx and vy must not have zero length")

    new Point2D.Double((p dot vx) / vxsq, (p dot vy) / vysq)
  }

  def crossProduct(p: Point2D, q: Point2D): Double = {
    var x1: Double = p.getX()
    var y1: Double = p.getY()
    var x2: Double = q.getX()
    var y2: Double = q.getY()
    return x1 * y2 - y1 * x2
  }
}
