package org.workcraft.dom.visual.connections

import java.awt.Shape
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import org.workcraft.exceptions.NotImplementedException
import org.workcraft.graphics.Java2DDecoration._
import org.workcraft.graphics.VisualCurveProperties
import org.workcraft.graphics.ParametricCurve
import org.workcraft.graphics.Geometry.lerp
import org.workcraft.graphics.Geometry.createRectangle
import org.workcraft.dom.visual.connections.VisualConnectionContext

object PolylineGui {

  def createPolylineControlPoint(connectionProps: VisualCurveProperties, userLocation: Point2D.Double) {
    //PVector<Point2D.Double> controlPoints = eval(mapM(ControlPoint.positionGetter).apply(eval(polyline.controlPoints())))
    //Curve curve = PolylineGui.curveMaker.apply(controlPoints, connectionProps)
    //Pair<Int, Double> lt = curve.getNearestPointTLocal(userLocation)
    //polyline.createControlPoint(lt.getFirst(), curve.getPoint(lt))
    throw new NotImplementedException()
  }

  class Curve(anchorPoints: List[Point2D.Double]) extends ParametricCurve {

    val anchorsArr = anchorPoints.toArray
    private val segmentCount = anchorPoints.length - 1

    def getLocalT(t: Double): (Int, Double) = {
      var segmentIndex = Math.floor(t * segmentCount).toInt

      if (segmentIndex == segmentCount) segmentIndex -= 1

      (segmentIndex, t * segmentCount - segmentIndex)
    }

    def getSegment(index: Int): Line2D.Double = {
      if (index >= segmentCount)
        throw new RuntimeException("Segment index is greater than number of segments")

      new Line2D.Double(anchorsArr(index), anchorsArr(index + 1))
    }

    override def derivativeAt(tt: Double): Point2D.Double = {
      var t = tt
      if (t < 0) t = 0
      if (t > 1) t = 1

      val segmentIndex = getLocalT(t)._1
      val segment = getSegment(segmentIndex)

      segment.p2 - segment.p1
    }

    override def secondDerivativeAt(t: Double): Point2D.Double =
      derivativeAt(t + 0.05) - derivativeAt(t - 0.05)

    override def pointOnCurve(t: Double): Point2D.Double = getPoint(getLocalT(t))

    private def getPoint(localTI: (Int, Double)): Point2D.Double = {
      val (i, t) = localTI
      val segment = getSegment(i)
      lerp(segment.p1, segment.p2, t)
    }

    def toGlobalT(pt: (Int, Double)) = (pt._1 + pt._2) / segmentCount

    override def nearestPointT(pt: Point2D.Double): Double = toGlobalT(getNearestPointTLocal(pt))

    private def clamp01(t: Double) = if (t < 0) 0 else if (t > 1) 1 else t

    def getNearestPointTLocal(pt: Point2D.Double): (Int, Double) = {
      var min = java.lang.Double.MAX_VALUE
      var bestT = (0, 0.0)

      Range(0, segmentCount).foreach(i => {
        val segment = getSegment(i)

        // We want to find a projection of a point PT onto a segment (P1, P2)
        // To do that, we shift the universe so that P1 == 0 and project the vector A = PT-P1 onto the vector B = P2-P1
        // We do that by dividing the dot product (A * B) by the squared magnitude of the vector B
        val a = pt - segment.p1
        val b = segment.p2 - segment.p1

        val magBSq = b.distanceSq(0, 0)

        // To avoid division by zero, we have a special case here
        val t =
          if (magBSq < 0.0000001) 0
          else clamp01((a dot b) / magBSq)

        val projected = b * t
        val dist = a.distance(projected)

        if (dist < min) {
          min = dist
          bestT = (i, t)
        }
      })

      bestT
    }

    private def getSegmentBounds(segment: Line2D.Double): Rectangle2D.Double =
      createRectangle(segment.p1, segment.p2)

    override def boundingBox = {
      Range(0, segmentCount).map(i => getSegmentBounds(getSegment(i)))
        .reduceLeft((_.createUnion(_))) // reduce is safe because polyLine always has at least one segment
    }

    override def shape(tStart: Double, tEnd: Double): Shape = getShape(getLocalT(tStart), getLocalT(tEnd))

    private def moveTo(path: Path2D, point: Point2D.Double) = path.moveTo(point.getX, point.getY)

    private def lineTo(path: Path2D.Double, point: Point2D.Double) = path.lineTo(point.getX, point.getY)

    private def getShape(start: (Int, Double), end: (Int, Double)): Shape = {
      val path = new Path2D.Double()
      moveTo(path, getPoint(start))
      Range(start._1, end._1).foreach(i => {
        val segment = getSegment(i)
        lineTo(path, segment.p2)
      })
      lineTo(path, getPoint(end))
      return path;
    }
  }

  def makeCurve(props: VisualCurveProperties, context: VisualConnectionContext, controlPoints: List[Point2D.Double]) =
    new Curve(context.c1.center :: (controlPoints :+ context.c2.center))
}
