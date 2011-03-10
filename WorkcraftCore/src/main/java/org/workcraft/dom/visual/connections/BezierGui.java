package org.workcraft.dom.visual.connections;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.dom.visual.connections.VisualConnectionGui.*;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.util.Function;
import org.workcraft.util.Function3;
import org.workcraft.util.Geometry;
import org.workcraft.util.Geometry.CurveSplitResult;

public class BezierGui {
	
	private static final Function<BezierGui, ColorisableGraphicalContent> graphicalContentGetter = new Function<BezierGui, ColorisableGraphicalContent>(){
		@Override
		public ColorisableGraphicalContent apply(BezierGui argument) {
			return argument.graphicalContent;
		}
	};
	
	private static final Function<BezierGui, Touchable> touchableGetter = new Function<BezierGui, Touchable>(){
		@Override
		public Touchable apply(BezierGui argument) {
			return argument.touchable;
		}
	};

	static Function3<VisualConnectionProperties, Point2D, Point2D, CubicCurve2D> fullCurve2DMaker = new Function3<VisualConnectionProperties, Point2D, Point2D, CubicCurve2D>(){
		@Override
		public CubicCurve2D apply(VisualConnectionProperties connectionInfo, Point2D cp1, Point2D cp2) {
			CubicCurve2D result = new CubicCurve2D.Double();
			result.setCurve(connectionInfo.getFirstShape().getCenter(), cp1, cp2, connectionInfo.getSecondShape().getCenter());
			return result;
		}
	};
	
	final ColorisableGraphicalContent graphicalContent;
	final Touchable touchable;

	public static Function3<Point2D, Point2D, VisualConnectionProperties, BezierGui> constuctor = new Function3<Point2D, Point2D, VisualConnectionProperties, BezierGui>() {
		@Override
		public BezierGui apply(Point2D argument1, Point2D argument2, VisualConnectionProperties argument3) {
			return new BezierGui(argument1, argument2, argument3);
		}
	};  
	
	public BezierGui(final Point2D cp1, final Point2D cp2, final VisualConnectionProperties connectionInfo) {
		final CubicCurve2D fullCurve2D = fullCurve2DMaker.apply(connectionInfo, cp1, cp2);
		final ParametricCurve parametricCurve = Curve.constructor.apply(fullCurve2D);
		final PartialCurveInfo curveInfo = curveInfoMaker.apply(connectionInfo, parametricCurve);
		final CubicCurve2D visibleCurve2D = getPartialCurve(fullCurve2D, curveInfo);
		graphicalContent = connectionGraphicalContentMaker.apply(curveInfo, connectionInfo, visibleCurve2D);
		touchable = connectionTouchableMaker.apply(parametricCurve);
	}
	
	private static CubicCurve2D getPartialCurve(
			final CubicCurve2D fullCurve2D,
			final PartialCurveInfo curve) {
		
			double tEnd = curve.tEnd;
			double tStart = curve.tStart;
		
			CubicCurve2D fullCurve = fullCurve2D;
			
			CurveSplitResult firstSplit = Geometry.splitCubicCurve(fullCurve, tStart);
			CurveSplitResult secondSplit = Geometry.splitCubicCurve(firstSplit.curve2, (tEnd-tStart)/(1-tStart));
			return secondSplit.curve1;
	}
	
	private final static class Curve implements ParametricCurve {

		public static Function<CubicCurve2D, Curve> constructor = new Function<CubicCurve2D, Curve>(){
			@Override
			public Curve apply(CubicCurve2D argument) {
				return new Curve(argument);
			}
		}; 
		
		public Curve(CubicCurve2D fullCurve2D) {
			this.fullCurve2D = fullCurve2D;
		}
		
		private final CubicCurve2D fullCurve2D;
		
		@Override
		public double getDistanceToCurve(Point2D pt) {
			return pt.distance(getNearestPointOnCurve(pt));
		}

		@Override
		public Point2D getNearestPointOnCurve(Point2D pt) {
			// FIXME: should be done using some proper algorithm
			Point2D nearest = new Point2D.Double(fullCurve2D.getX1(), fullCurve2D.getY1());
			double nearestDist = Double.MAX_VALUE;
			
			for (double t=0.01; t<=1.0; t+=0.01) {
				Point2D samplePoint = Geometry.getPointOnCubicCurve(fullCurve2D, t);
				double distance = pt.distance(samplePoint);
				if (distance < nearestDist)	{
					nearestDist = distance;
					nearest = samplePoint;
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

/*	public void setDefaultControlPoints() {
		Expression<Point2D> p1 = origin1();
		Expression<Point2D> p2 = new ExpressionBase<Point2D>() {
			@Override
			protected Point2D evaluate(EvaluationContext context) {
				return context.resolve(connectionInfo).getSecondShape().getCenter();
			}
		};
		
		BezierControlPoint cp1 = new BezierControlPoint(p1, storage);
		BezierControlPoint cp2 = new BezierControlPoint(p2, storage);
		initControlPoints (cp1, cp2);

		Point2D c1 = eval(p1);
		Point2D c2 = eval(p2);
		cp1.position().setValue(Geometry.lerp(c1, c2, 0.3));
		cp2.position().setValue(Geometry.lerp(c1, c2, 0.6));
		
		finaliseControlPoints();
	}*/

	public static Expression<? extends ColorisableGraphicalContent> getGraphicalContent(BezierConfiguration bezier, Expression<? extends VisualConnectionProperties> connectionProperties) {
		return bindFunc(bindFunc(bezier.controlPoint1(), bezier.controlPoint2(), connectionProperties, constuctor), graphicalContentGetter);
	}

	public static Expression<? extends Touchable> shape(BezierConfiguration bezier, Expression<VisualConnectionProperties> properties) {
		return bindFunc(bindFunc(bezier.controlPoint1(), bezier.controlPoint2(), properties, constuctor), touchableGetter);
	}
}
