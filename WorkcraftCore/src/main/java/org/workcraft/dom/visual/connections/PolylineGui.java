package org.workcraft.dom.visual.connections;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.util.Geometry;

public class PolylineGui implements ConnectionGraphic {
	private Expression<? extends VisualConnectionProperties> connectionInfo;
	
	public PolylineGui (Expression<? extends VisualConnectionProperties> properties) {
		connectionInfo = properties;
	}
	
	
	private final class CurveExpression extends ExpressionBase<Curve> {
		@Override
		public Curve evaluate(final EvaluationContext resolver) {
			return new Curve(resolver);
		}
	}

	private final class Curve implements ParametricCurve {
		
		AnchorPointsExpression anchorPoints = new AnchorPointsExpression();
		private final class AnchorPointsExpression extends ExpressionBase<List<Point2D>> {

			@Override
			public List<Point2D> evaluate(EvaluationContext resolver) {
				List<Point2D> result = new ArrayList<Point2D>();
				result.add(resolver.resolve(connectionInfo).getFirstShape().getCenter());
				LinkedList<Node> children = resolver.resolve(groupImpl.children());
				for(Node child : children)
					result.add(resolver.resolve(((ControlPoint) child).position()));
				result.add(resolver.resolve(connectionInfo).getSecondShape().getCenter());
				return result;
			}
		}

		private final EvaluationContext resolver;

		private Curve(EvaluationContext resolver) {
			this.resolver = resolver;
		}

		private int getSegmentCount() {
			return controlPoints().size() + 1;
		}

		private Line2D getSegment(final int index) {
			int segments = getSegmentCount();

			if (index > segments-1)
				throw new RuntimeException ("Segment index is greater than number of segments");

			List<Point2D> anchorPoints = resolver.resolve(this.anchorPoints);
			return new Line2D.Double(anchorPoints.get(index), anchorPoints.get(index+1));
		}

		private List<Node> controlPoints() {
			return resolver.resolve(groupImpl.children());
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
	}

	private final class CurveInfo extends ExpressionBase<PartialCurveInfo> {
		@Override
		public PartialCurveInfo evaluate(EvaluationContext resolver) {
			return Geometry.buildConnectionCurveInfo(resolver.resolve(connectionInfo), resolver.resolve(curve), 0);
		}
	}


	@Override
	public ExpressionBase<ColorisableGraphicalContent> graphicalContent() {
		return new ExpressionBase<ColorisableGraphicalContent>() {
			@Override
			public ColorisableGraphicalContent evaluate(EvaluationContext resolver) {
				
				final Path2D connectionPath = new Path2D.Double();

				final PartialCurveInfo cInfo = resolver.resolve(curveInfo);
				
				Curve curve = resolver.resolve(curve());
				
				int start = curve.getSegmentIndex(cInfo.tStart);
				int end = curve.getSegmentIndex(cInfo.tEnd);

				Point2D startPt = curve.getPointOnCurve(cInfo.tStart);
				Point2D endPt = curve.getPointOnCurve(cInfo.tEnd);

				connectionPath.moveTo(startPt.getX(), startPt.getY());

				for (int i=start; i<end; i++) {
					Line2D segment = curve.getSegment(i);
					connectionPath.lineTo(segment.getX2(), segment.getY2());
				}

				connectionPath.lineTo(endPt.getX(), endPt.getY());
				
				final VisualConnectionProperties connInfo = resolver.resolve(connectionInfo);

				return new ColorisableGraphicalContent() {
					
					@Override
					public void draw(DrawRequest r) {
						Graphics2D g = r.getGraphics();
						
						Color color = Coloriser.colorise(connInfo.getDrawColor(), r.getColorisation().getColorisation());
						g.setColor(color);
						g.setStroke(connInfo.getStroke());
						g.draw(connectionPath);
						
						if (connInfo.hasArrow())
							DrawHelper.drawArrowHead(g, color, cInfo.arrowHeadPosition, cInfo.arrowOrientation, 
									connInfo.getArrowLength(), connInfo.getArrowWidth());
					}
				};
			}
		};
	}

	@Override
	public Expression<? extends Touchable> shape() {
		return new ExpressionBase<Touchable>(){

			@Override
			protected Touchable evaluate(final EvaluationContext context) {
				return new Touchable() {

					@Override
					public boolean hitTest(Point2D point) {
						return context.resolve(curve).getDistanceToCurve(point) < VisualConnection.HIT_THRESHOLD;
					}
					@Override
					public Point2D getCenter()
					{
						return context.resolve(curve).getPointOnCurve(0.5);
					}
					
					@Override
					public Rectangle2D getBoundingBox() {
						return context.resolve(curve).getBoundingBox();
					}
				};
			}
			
		};
	}

	private ExpressionBase<PartialCurveInfo> curveInfo = new CurveInfo();
	
	private ExpressionBase<VisualConnectionProperties> connectionInfo;
	connectionInfo = parent.properties();
	ControlPointScaler scaler;
	scaler = new ControlPointScaler(connectionInfo, controlPoints());

	

	public void createControlPoint(Point2D point) {
		Point2D pointOnConnection = new Point2D.Double();
		int segment = GlobalCache.eval(curve()).getNearestSegment(userLocation, pointOnConnection);

		createControlPoint(segment, pointOnConnection);
	}


	private Expression<? extends Collection<? extends ControlPoint>> controlPoints() {
		return new ExpressionBase<Collection<? extends ControlPoint>>() {
			@Override
			protected Collection<? extends ControlPoint> evaluate(EvaluationContext context) {
				ArrayList<ControlPoint> points = new ArrayList<ControlPoint>();
				for(Node n : context.resolve(children())) {
					points.add((ControlPoint)n);
				}
				return points;
			}
		};
	}
	
}
