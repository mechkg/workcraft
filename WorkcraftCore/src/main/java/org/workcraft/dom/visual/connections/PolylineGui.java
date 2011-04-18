package org.workcraft.dom.visual.connections;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.util.Function2;
import org.workcraft.util.Geometry;
import org.workcraft.util.Pair;

import pcollections.PVector;

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
	
	public static void createPolylineControlPoint(VisualConnectionProperties connectionProps, Polyline polyline, Point2D userLocation) {
		PVector<Point2D> controlPoints = eval(mapM(ControlPoint.positionGetter).apply(eval(polyline.controlPoints())));
		//Curve curve = PolylineGui.curveMaker.apply(controlPoints, connectionProps);
		//Pair<Integer, Double> lt = curve.getNearestPointTLocal(userLocation);
		//polyline.createControlPoint(lt.getFirst(), curve.getPoint(lt));
		throw new NotImplementedException();
	}
	
	public static final class Curve implements ParametricCurve {
		public List<Point2D> anchorPoints;

		private Curve(List<Point2D> anchorPoints) {
			this.anchorPoints = anchorPoints;
		}

		public Pair<Integer, Double> getLocalT(double t) {
			int segments = getSegmentCount();
			
			int segmentIndex = (int)Math.floor(t*segments);
			if (segmentIndex==segments) segmentIndex -= 1;
			
			return Pair.of(segmentIndex, t * getSegmentCount() - segmentIndex);
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

		@Override
		public Point2D getDerivativeAt(double t)
		{
			if (t < 0) t = 0;
			if (t > 1) t = 1;
			
			int segmentIndex = getLocalT(t).getFirst();
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
			Pair<Integer, Double> localTI = getLocalT(t);
			return getPoint(localTI);
		}
		
		private Point2D getPoint(Pair<Integer, Double> localTI) {
			int i = localTI.getFirst();
			double t = localTI.getSecond();

			Line2D segment = getSegment(i);
			return Geometry.lerp(segment.getP1(), segment.getP2(), t);
		}

		public double toGlobalT(Pair<Integer, Double> pt) {
			return (pt.getFirst() + pt.getSecond()) / getSegmentCount();
		}

		@Override
		public double getNearestPointT(Point2D pt) {
			return toGlobalT(getNearestPointTLocal(pt));
		}
		
		public Pair<Integer,Double> getNearestPointTLocal(Point2D pt) {
			double min = Double.MAX_VALUE;
			Pair<Integer, Double> bestT = Pair.of(0, 0.0);
			
			int segmentCount = getSegmentCount();
			for (int i=0; i<segmentCount; i++) {
				Line2D segment = getSegment(i);

				// We want to find a projection of a point PT onto a segment (P1, P2)
				// To do that, we shift the universe so that P1 == 0 and project the vector A = PT-P1 onto the vector B = P2-P1
				// We do that by dividing the dot product (A * B) by the squared magnitude of the vector B
				Point2D a = Geometry.subtract(pt, segment.getP1());
				Point2D b = Geometry.subtract(segment.getP2(), segment.getP1());

				double magBSq = b.distanceSq(0, 0);

				double dist;
				double t;

				// To avoid division by zero, we have a special case here
				if (magBSq < 0.0000001) {
					t = 0;
				} else {
					t = Geometry.dotProduct(a, b) / magBSq;
					if (t < 0)
						t = 0;
					if (t > 1)
						t = 1;
				}
				
				Point2D projected = Geometry.multiply(b, t);
				dist = a.distance(projected);
				
				if (dist < min) {
					min = dist;
					bestT = Pair.of(i, t);
				}
			}

			return bestT;
		}
		
		private static Rectangle2D getSegmentBounds (Line2D segment) {
			return Geometry.createRectangle(segment.getP1(), segment.getP2());
		}

		@Override
		public Rectangle2D getBoundingBox() {
			int segments = getSegmentCount();

			Rectangle2D result = null;
			for (int i=0; i < segments; i++) {
				result = BoundingBoxHelper.union(result, getSegmentBounds(getSegment(i)));
			}
			return result;
		}
		
		@Override
		public Shape getShape(double tStart, double tEnd) {
			return getShape(getLocalT(tStart), getLocalT(tEnd));
		}

		void moveTo(Path2D path, Point2D point) {
			path.moveTo(point.getX(), point.getY());
		}
		
		void lineTo(Path2D path, Point2D point) {
			path.lineTo(point.getX(), point.getY());
		}
		
		private Shape getShape(Pair<Integer, Double> start, Pair<Integer, Double> end) {
			final Path2D path = new Path2D.Double();
			moveTo(path, getPoint(start));
			for (int i=start.getFirst(); i<end.getFirst(); i++) {
				Line2D segment = getSegment(i);
				lineTo(path, segment.getP2());
			}
			lineTo(path, getPoint(end));
			return path;		
		}
	}

	public static Curve makeCurve(VisualConnectionProperties props, VisualConnectionContext context, List<? extends Point2D> controlPoints) {
		return new Curve(createAnchorPoints(context.component1().getCenter(), context.component2().getCenter(), controlPoints));
	}
}
