/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.dom.visual.connections;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.DeprecatedGraphicalContent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.gui.Coloriser;
import org.workcraft.util.ExpressionUtil;
import org.workcraft.util.Geometry;
import org.workcraft.util.Geometry.CurveSplitResult;

public class Bezier implements ConnectionGraphic, SelectionObserver {

	private final Expression<PartialCurveInfo> curveInfo;
	private final Expression<VisualConnectionProperties> connectionInfo;
	private final Expression<CubicCurve2D> fullCurve2D;
	private final Expression<CubicCurve2D> visibleCurve2D;
	private final Expression<? extends ParametricCurve> parametricCurve;
	
	private Node parent;
	private final ModifiableExpression<BezierControlPoint> cp1;
	private final ModifiableExpression<BezierControlPoint> cp2;
	private final ControlPointScaler scaler;
	public final StorageManager storage;
	
	@Override
	public ControlPointScaler scaler() {
		return scaler;
	}
	
	public Bezier(VisualConnection parent, StorageManager storage) {
		this.storage = storage;
		cp1 = storage.create(null);
		cp2 = storage.create(null);
		
		this.connectionInfo = parent.properties();
		this.parent = parent;
		this.scaler = new ControlPointScaler(connectionInfo, children());
		
		parametricCurve = new ExpressionBase<ParametricCurve>() {

			@Override
			protected ParametricCurve evaluate(EvaluationContext context) {
				return new Curve(context);
			}
		};
		
		this.curveInfo = new ExpressionBase<PartialCurveInfo>() {
			@Override
			protected PartialCurveInfo evaluate(EvaluationContext context) {
				return Geometry.buildConnectionCurveInfo(context.resolve(connectionInfo), context.resolve(parametricCurve), 0); }
		};

		this.fullCurve2D = new ExpressionBase<CubicCurve2D>(){
			@Override
			public CubicCurve2D evaluate(org.workcraft.dependencymanager.advanced.core.EvaluationContext resolver) {
				CubicCurve2D result = new CubicCurve2D.Double();
				result.setCurve(resolver.resolve(connectionInfo).getFirstShape().getCenter(), resolver.resolve(resolver.resolve(cp1).position()), resolver.resolve(resolver.resolve(cp2).position()), resolver.resolve(connectionInfo).getSecondShape().getCenter());
				return result;
			};
		};
		this.visibleCurve2D = getPartialCurve(fullCurve2D, curveInfo);
	}
	
	@Override
	public Expression<? extends ParametricCurve> curve() {
		return parametricCurve;
	}
	
	@Override
	public ModifiableExpression<Node> parent() {
		return ExpressionUtil.modificationNotSupported(parentConnection());
	}

	private ExpressionBase<Node> parentConnection() {
		return new ExpressionBase<Node>() {
			@Override
			protected Node evaluate(EvaluationContext context) {
				return parent;
			} 
		};
	}

	public void setDefaultControlPoints() {
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
	}

	public ExpressionBase<Point2D> origin1() {
		return new ExpressionBase<Point2D>() {
			@Override
			protected Point2D evaluate(EvaluationContext context) {
				return context.resolve(connectionInfo).getFirstShape().getCenter();
			}
		};
	}
	
	public ExpressionBase<Point2D> origin2() {
		return new ExpressionBase<Point2D>() {
			@Override
			protected Point2D evaluate(EvaluationContext context) {
				return context.resolve(connectionInfo).getSecondShape().getCenter();
			}
		};
	}
	
	public void initControlPoints(BezierControlPoint cp1, BezierControlPoint cp2) {
		this.cp1.setValue(cp1);
		this.cp2.setValue(cp2);
	}
	
	public void finaliseControlPoints() {
		eval(cp1).parent().setValue(this);
		eval(cp2).parent().setValue(this);
	}
	
	public Expression<BezierControlPoint[]> getControlPoints() {
		return new ExpressionBase<BezierControlPoint[]>(){

			@Override
			protected BezierControlPoint[] evaluate(EvaluationContext context) {
				return new BezierControlPoint[] { context.resolve(cp1), context.resolve(cp2) };
			}
		};
	}
	
	@Override
	public Expression<? extends DeprecatedGraphicalContent> graphicalContent() {
		return new ExpressionBase<DeprecatedGraphicalContent>() {
			@Override
			protected DeprecatedGraphicalContent evaluate(final EvaluationContext context) {
				return new DeprecatedGraphicalContent() {
					@Override
					public void draw(DrawRequest r) {
						Graphics2D g = r.getGraphics();
						
						VisualConnectionProperties cinfo = context.resolve(connectionInfo);
						Color color = Coloriser.colorise(cinfo.getDrawColor(), r.getDecoration().getColorisation());
						g.setColor(color);
//						g.setStroke(new BasicStroke((float)connectionInfo.getLineWidth()));
						g.setStroke(cinfo.getStroke());
						
						g.draw(context.resolve(visibleCurve2D));
						PartialCurveInfo cvInfo = context.resolve(curveInfo);
						if (cinfo.hasArrow())
							DrawHelper.drawArrowHead(g, color,
									cvInfo.arrowHeadPosition,
									cvInfo.arrowOrientation,
									cinfo.getArrowLength(),
									cinfo.getArrowWidth());
					}
				};
			}
		};
	}

	private static Expression<CubicCurve2D> getPartialCurve(
			final Expression<? extends CubicCurve2D> fullCurve2D,
			final Expression<? extends PartialCurveInfo> curveInfo) {
		
		return new ExpressionBase<CubicCurve2D>() {
			@Override
				protected CubicCurve2D evaluate(EvaluationContext context) {
					PartialCurveInfo curve = context.resolve(curveInfo);
					double tEnd = curve.tEnd;
					double tStart = curve.tStart;
				
					CubicCurve2D fullCurve = context.resolve(fullCurve2D);
					
					CurveSplitResult firstSplit = Geometry.splitCubicCurve(fullCurve, tStart);
					CurveSplitResult secondSplit = Geometry.splitCubicCurve(firstSplit.curve2, (tEnd-tStart)/(1-tStart));
					return secondSplit.curve1;
				}
		};
	}
	
	@Override
	public Expression<? extends Collection<? extends ControlPoint>> children() {
		return new ExpressionBase<List<BezierControlPoint>>(){
			@Override
			protected List<BezierControlPoint> evaluate(
					EvaluationContext context) {
				return Arrays.asList(context.resolve(getControlPoints()));
			}
		};
	}
	
	@Override
	public Expression<? extends Touchable> shape() {
		return new ExpressionBase<Touchable>() {

			@Override
			protected Touchable evaluate(final EvaluationContext context) {
				return new Touchable() {

					@Override
					public boolean hitTest(Point2D point) {
						return context.resolve(parametricCurve).getDistanceToCurve(point) < VisualConnection.HIT_THRESHOLD;
					}

					@Override
					public Rectangle2D getBoundingBox() {
						return context.resolve(parametricCurve).getBoundingBox();
					}

					@Override
					public Point2D getCenter() {
						return context.resolve(parametricCurve).getPointOnCurve(0.5);
					}
					
				};
			}
		};
	}
	
	private final class Curve implements ParametricCurve {

		public Curve(EvaluationContext resolver) {
			this.resolver = resolver;
		}
		
		private final EvaluationContext resolver;
		
		@Override
		public double getDistanceToCurve(Point2D pt) {
			return pt.distance(getNearestPointOnCurve(pt));
		}

		@Override
		public Point2D getNearestPointOnCurve(Point2D pt) {
			// FIXME: should be done using some proper algorithm
			
			Point2D nearest = new Point2D.Double(resolver.resolve(fullCurve2D).getX1(), resolver.resolve(fullCurve2D).getY1());
			double nearestDist = Double.MAX_VALUE;
			
			for (double t=0.01; t<=1.0; t+=0.01) {
				Point2D samplePoint = Geometry.getPointOnCubicCurve(resolver.resolve(fullCurve2D), t);
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
			return Geometry.getPointOnCubicCurve(resolver.resolve(fullCurve2D), t);
		}
		@Override
		public Point2D getDerivativeAt(double t) {
			return Geometry.getDerivativeOfCubicCurve(resolver.resolve(fullCurve2D), t);
		}

		@Override
		public Point2D getSecondDerivativeAt(double t) {
			return Geometry.getSecondDerivativeOfCubicCurve(resolver.resolve(fullCurve2D), t);
		}
		
		@Override
		public Rectangle2D getBoundingBox() {
			Rectangle2D boundingBox = resolver.resolve(fullCurve2D).getBounds2D();
			boundingBox.add(boundingBox.getMinX()-VisualConnection.HIT_THRESHOLD, boundingBox.getMinY()-VisualConnection.HIT_THRESHOLD);
			boundingBox.add(boundingBox.getMinX()-VisualConnection.HIT_THRESHOLD, boundingBox.getMaxY()+VisualConnection.HIT_THRESHOLD);
			boundingBox.add(boundingBox.getMaxX()+VisualConnection.HIT_THRESHOLD, boundingBox.getMinY()-VisualConnection.HIT_THRESHOLD);
			boundingBox.add(boundingBox.getMaxX()+VisualConnection.HIT_THRESHOLD, boundingBox.getMaxY()+VisualConnection.HIT_THRESHOLD);
			return boundingBox;
		}
	}

	Variable<Expression<? extends Collection<? extends Node>>> selectionTracker = new Variable<Expression<? extends Collection<? extends Node>>>(Expressions.constant(Collections.<Node>emptyList()));
	
	Expression<Boolean> controlsHidden = new ExpressionBase<Boolean>(){
		@Override
		protected Boolean evaluate(EvaluationContext context) {
			boolean controlsVisible = true;
			for (Node n : context.resolve(context.resolve(selectionTracker)))
				if (n==context.resolve(cp1) || n == context.resolve(cp2) || n == parent) {
					controlsVisible = false;
					break;
				}
			return controlsVisible;
		}
	};
	
	public Expression<Boolean> controlsHidden() {
		return controlsHidden;
	}

	@Override
	public void setSelection(Expression<? extends Collection<? extends Node>> selection) {
		// only called once
		selectionTracker.setValue(selection);
	}
}
