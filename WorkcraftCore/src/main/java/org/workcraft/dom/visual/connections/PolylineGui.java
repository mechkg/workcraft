package org.workcraft.dom.visual.connections;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.util.Function2;
import org.workcraft.util.Geometry;

public class PolylineGui {


	public static List<Point2D> createAnchorPoints(final Point2D first, final Point2D second, final List<? extends Point2D> controlPoints) {
		List<Point2D> result = new ArrayList<Point2D>();
		result.add(first);
		List<? extends Point2D> children = controlPoints;
		for(Point2D child : children)
			result.add(child);
		result.add(second);
		return result;
	}
	
	private static final class Curve implements ParametricCurve {
		private List<Point2D> anchorPoints;

		private Curve(List<Point2D> anchorPoints) {
			this.anchorPoints = anchorPoints;
		}

		private int getSegmentCount() {
			return anchorPoints.size() - 1;
		}

		private Line2D getSegment(final int index) {
			int segments = getSegmentCount();

			if (index > segments-1)
				throw new RuntimeException ("Segment index is greater than number of segments");

			return new Line2D.Double(anchorPoints.get(index), anchorPoints.get(index+1));
		}

		private int getSegmentIndex(final double t) {
			int segments = getSegmentCount();
			double l = 1.0 / segments;
			double t_l = t/l;

			int n = (int)Math.floor(t_l);
			if (n==segments) n -= 1;
			return n;
		}

		private double getParameterOnSegment (double t, int segmentIndex) {
			return t * getSegmentCount() - segmentIndex;
		}

		@Override
		public Point2D getDerivativeAt(double t)
		{
			if (t < 0) t = 0;
			if (t > 1) t = 1;
			
			int segmentIndex = getSegmentIndex(t);
			Line2D segment = getSegment(segmentIndex);

			return Geometry.subtract(segment.getP2(), segment.getP1());
		}

		@Override
		public Point2D getSecondDerivativeAt(double t)
		{		
			Point2D left = getDerivativeAt(t - 0.05);
			Point2D right = getDerivativeAt(t + 0.05);

			return Geometry.subtract(right, left);
		}

		@Override
		public Point2D getPointOnCurve(double t) {
			int segmentIndex = getSegmentIndex(t);
			double t2 = getParameterOnSegment(t, segmentIndex);

			Line2D segment = getSegment(segmentIndex);

			return new Point2D.Double(segment.getP1().getX() * (1-t2) + segment.getP2().getX() * t2,
					segment.getP1().getY() * (1-t2) + segment.getP2().getY() * t2);
		}

		@Override
		public Point2D getNearestPointOnCurve(Point2D pt) {
			Point2D result = new Point2D.Double();
			getNearestSegment(pt, result);
			return result;	
		}

		private int getNearestSegment (Point2D pt, Point2D out_pointOnSegment) {
			double min = Double.MAX_VALUE;
			int nearest = -1;

			for (int i=0; i<getSegmentCount(); i++) {
				Line2D segment = getSegment(i);

				Point2D a = new Point2D.Double ( pt.getX() - segment.getX1(), pt.getY() - segment.getY1() );
				Point2D b = new Point2D.Double ( segment.getX2() - segment.getX1(), segment.getY2() - segment.getY1() );

				double magB = b.distance(0, 0);

				double dist;

				if (magB < 0.0000001) {
					dist = pt.distance(segment.getP1());
				} else {
					b.setLocation(b.getX() / magB, b.getY() / magB);

					double magAonB = a.getX() * b.getX() + a.getY() * b.getY();

					if (magAonB < 0)
						magAonB = 0;
					if (magAonB > magB)
						magAonB = magB;

					a.setLocation(segment.getX1() + b.getX() * magAonB, segment.getY1() + b.getY() * magAonB);

					dist = new Point2D.Double(pt.getX() - a.getX(), pt.getY() - a.getY()).distance(0,0);
				}

				if (dist < min) {
					min = dist;
					if (out_pointOnSegment != null)
						out_pointOnSegment.setLocation(a);
					nearest = i;
				}
			}

			return nearest;
		}

		private Rectangle2D getSegmentBoundsWithThreshold (Line2D segment) {
			Point2D pt1 = segment.getP1();
			Point2D pt2 = segment.getP2();

			Rectangle2D bb = new Rectangle2D.Double(pt1.getX(), pt1.getY(), 0, 0);
			bb.add(pt2);
			Point2D lineVec = new Point2D.Double(pt2.getX() - pt1.getX(), pt2.getY() - pt1.getY());

			double mag = lineVec.distance(0, 0);

			if (mag==0)
				return bb;

			lineVec.setLocation(lineVec.getY()*VisualConnection.HIT_THRESHOLD/mag, -lineVec.getX()*VisualConnection.HIT_THRESHOLD/mag);
			bb.add(pt1.getX() + lineVec.getX(), pt1.getY() + lineVec.getY());
			bb.add(pt2.getX() + lineVec.getX(), pt2.getY() + lineVec.getY());
			bb.add(pt1.getX() - lineVec.getX(), pt1.getY() - lineVec.getY());
			bb.add(pt2.getX() - lineVec.getX(), pt2.getY() - lineVec.getY());

			return bb;
		}

		@Override
		public double getDistanceToCurve(Point2D pt) {
			double min = Double.MAX_VALUE;
			for (int i=0; i<getSegmentCount(); i++) {
				Line2D segment = getSegment(i);
				double dist = segment.ptSegDist(pt);
				if (dist < min)
					min = dist;
			}
			return min;
		}

		@Override
		public Rectangle2D getBoundingBox() {
			int segments = getSegmentCount();

			Rectangle2D result = null;
			for (int i=0; i < segments; i++) {
				Line2D seg = getSegment(i);
				result = BoundingBoxHelper.union(result, getSegmentBoundsWithThreshold(seg));
			}
			return result;
		}
		
		@Override
		public Shape getShape(double tStart, double tEnd) {
			int start = getSegmentIndex(tStart);
			int end = getSegmentIndex(tEnd);

			Point2D startPt = getPointOnCurve(tStart);
			Point2D endPt = getPointOnCurve(tEnd);

			final Path2D connectionPath = new Path2D.Double();
			connectionPath.moveTo(startPt.getX(), startPt.getY());
			for (int i=start; i<end; i++) {
				Line2D segment = getSegment(i);
				connectionPath.lineTo(segment.getX2(), segment.getY2());
			}
			connectionPath.lineTo(endPt.getX(), endPt.getY());
			return connectionPath;
		}
	}

	public final static Function2<List<? extends Point2D>, VisualConnectionProperties, Curve> curveMaker = new  Function2<List<? extends Point2D>, VisualConnectionProperties, Curve>(){
		@Override
		public Curve apply(List<? extends Point2D> controlPoints, VisualConnectionProperties props) {
			return new Curve(createAnchorPoints(props.getFirstShape().getCenter(), props.getSecondShape().getCenter(), controlPoints));
		}
	};


/*	ControlPointScaler scaler;
	scaler = new ControlPointScaler(connectionInfo, controlPoints());

	public void createControlPoint(Point2D point) {
		Point2D pointOnConnection = new Point2D.Double();
		int segment = GlobalCache.eval(curve()).getNearestSegment(userLocation, pointOnConnection);

		createControlPoint(segment, pointOnConnection);
	}*/
}
