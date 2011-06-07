package org.workcraft.dom.visual.connections;

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.util.Geometry;
import org.workcraft.util.Geometry.CurveSplitResult;

public class BezierGui {
	
	public static ParametricCurve makeCurve(VisualConnectionProperties connectionInfo, VisualConnectionContext context, RelativePoint cp1, RelativePoint cp2) {
		CubicCurve2D curve2D = new CubicCurve2D.Double();
		Point2D c1 = context.component1().getCenter();
		Point2D c2 = context.component2().getCenter();
		Point2D absoluteCp1 = cp1.toSpace(c1, c2);
		Point2D absoluteCp2 = cp2.toSpace(c1, c2);
		curve2D.setCurve(c1, absoluteCp1, absoluteCp2, c2);
		return new Curve(curve2D);
	}
	
	private final static class Curve implements ParametricCurve {
		
		@Override
		public Shape getShape(double tStart, double tEnd) {
			CurveSplitResult firstSplit = Geometry.splitCubicCurve(fullCurve2D, tStart);
			CurveSplitResult secondSplit = Geometry.splitCubicCurve(firstSplit.curve2, (tEnd-tStart)/(1-tStart));
			return secondSplit.curve1;
		}
		
		public Curve(CubicCurve2D fullCurve2D) {
			this.fullCurve2D = fullCurve2D;
		}
		
		private final CubicCurve2D fullCurve2D;
		
		@Override
		public double getNearestPointT(Point2D pt) {
			// FIXME: should be done using some proper algorithm
			Double nearest = 0.0;
			double nearestDist = Double.MAX_VALUE;
			
			for (double t=0.01; t<=1.0; t+=0.01) {
				Point2D samplePoint = Geometry.getPointOnCubicCurve(fullCurve2D, t);
				double distance = pt.distance(samplePoint);
				if (distance < nearestDist)	{
					nearestDist = distance;
					nearest = t;
				}
			}
			
			return nearest;
		}

		@Override
		public Point2D getPointOnCurve(double t) {
			return Geometry.getPointOnCubicCurve(fullCurve2D, t);
		}
		@Override
		public Point2D getDerivativeAt(double t) {
			return Geometry.getDerivativeOfCubicCurve(fullCurve2D, t);
		}

		@Override
		public Point2D getSecondDerivativeAt(double t) {
			return Geometry.getSecondDerivativeOfCubicCurve(fullCurve2D, t);
		}
		
		@Override
		public Rectangle2D getBoundingBox() {
			Rectangle2D boundingBox = fullCurve2D.getBounds2D();
			boundingBox.add(boundingBox.getMinX()-VisualConnection.HIT_THRESHOLD, boundingBox.getMinY()-VisualConnection.HIT_THRESHOLD);
			boundingBox.add(boundingBox.getMinX()-VisualConnection.HIT_THRESHOLD, boundingBox.getMaxY()+VisualConnection.HIT_THRESHOLD);
			boundingBox.add(boundingBox.getMaxX()+VisualConnection.HIT_THRESHOLD, boundingBox.getMinY()-VisualConnection.HIT_THRESHOLD);
			boundingBox.add(boundingBox.getMaxX()+VisualConnection.HIT_THRESHOLD, boundingBox.getMaxY()+VisualConnection.HIT_THRESHOLD);
			return boundingBox;
		}
	}
}
