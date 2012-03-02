package org.workcraft.dom.visual.connections

import java.awt.Shape
import java.awt.geom.CubicCurve2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import org.workcraft.util.Geometry
import org.workcraft.util.Geometry.CurveSplitResult

import org.workcraft.graphics.Java2DDecoration._

object BezierGui {
  
  def makeCurve(connectionInfo : VisualConnectionProperties, context : VisualConnectionContext, cp1 : RelativePoint, cp2 : RelativePoint) : ParametricCurve = {
    val curve2D = new CubicCurve2D.Double
    val c1 = context.c1.center
    val c2 = context.c2.center
    val absoluteCp1 = cp1.toSpace(c1, c2)
    val absoluteCp2 = cp2.toSpace(c1, c2)
    curve2D.setCurve(c1, absoluteCp1, absoluteCp2, c2)
    new Curve(curve2D)
  }
  
  private class Curve(fullCurve2D : CubicCurve2D.Double) extends ParametricCurve {
    
    override def getShape(tStart : Double, tEnd : Double) : Shape = {
      val firstSplit = Geometry.splitCubicCurve(fullCurve2D, tStart)
      val secondSplit = Geometry.splitCubicCurve(firstSplit.curve2, (tEnd-tStart)/(1-tStart))
      secondSplit.curve1
    }
    
    override def getNearestPointT(pt : Point2D.Double) : Double = {
      // FIXME: should be done using some proper algorithm
      var nearest = 0.0
      var nearestDist = Double.MaxValue
      
      Range.Double(0.01, 1.0, 0.01).foreach(t => {
        val samplePoint = Geometry.getPointOnCubicCurve(fullCurve2D, t)
        val distance = pt.distance(samplePoint)
        if (distance < nearestDist)  {
          nearestDist = distance
          nearest = t
        }
      })
      
      nearest
    }

    override def getPointOnCurve(t : Double) : Point2D.Double =
      Geometry.getPointOnCubicCurve(fullCurve2D, t)

    override def getDerivativeAt(t : Double) : Point2D.Double =
      return Geometry.getDerivativeOfCubicCurve(fullCurve2D, t)

    override def getSecondDerivativeAt(t : Double) : Point2D.Double =
      Geometry.getSecondDerivativeOfCubicCurve(fullCurve2D, t)
    
    override def getBoundingBox : Rectangle2D.Double = {
      val boundingBox = fullCurve2D.bounds
      boundingBox.add(boundingBox.getMinX-VisualConnectionGui.HitThreshold, boundingBox.getMinY-VisualConnectionGui.HitThreshold)
      boundingBox.add(boundingBox.getMinX-VisualConnectionGui.HitThreshold, boundingBox.getMaxY+VisualConnectionGui.HitThreshold)
      boundingBox.add(boundingBox.getMaxX+VisualConnectionGui.HitThreshold, boundingBox.getMinY-VisualConnectionGui.HitThreshold)
      boundingBox.add(boundingBox.getMaxX+VisualConnectionGui.HitThreshold, boundingBox.getMaxY+VisualConnectionGui.HitThreshold)
      boundingBox
    }
  }
}
