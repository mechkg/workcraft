package org.workcraft.dom.visual.connections;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.Combinator2;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.Coloriser;
import org.workcraft.util.Function;
import org.workcraft.util.Function2;
import org.workcraft.util.Function3;
import org.workcraft.util.Geometry;

import pcollections.PVector;

public class VisualConnectionGui {

	public interface ConnectionGui {
		Expression<? extends Touchable> shape();
		Expression<? extends ColorisableGraphicalContent> graphicalContent();
		Expression<? extends ParametricCurve> parametricCurve();
	}

	private static final Combinator2<ConnectionGraphicConfiguration, Expression<VisualConnectionProperties>, ParametricCurve> curveMaker = new Combinator2<ConnectionGraphicConfiguration, Expression<VisualConnectionProperties>, ParametricCurve>() {
		
		@Override
		public Expression<? extends ParametricCurve> apply(ConnectionGraphicConfiguration config, final Expression<VisualConnectionProperties> connProps) {
			return config.accept(new ConnectionGraphicConfigurationVisitor<Expression<? extends ParametricCurve>>() {

				@Override
				public Expression<? extends ParametricCurve> visitPolyline(Polyline polyline) {
					final Expression<? extends Collection<? extends ControlPoint>> controlPointControls = polyline.controlPoints();
					Expression<PVector<Point2D>> controlPoints = bind(controlPointControls, mapM(ControlPoint.positionGetter));
					return bindFunc(controlPoints, connProps, PolylineGui.curveMaker);
				}

				@Override
				public Expression<? extends ParametricCurve> visitBezier(Bezier bezier) {
					Expression<? extends Point2D> p1 = bind(bezier.cp1, VisualTransformableNode.positionGetter);
					Expression<? extends Point2D> p2 = bind(bezier.cp2, VisualTransformableNode.positionGetter);
					return bindFunc(connProps, p1, p2, BezierGui.curveMaker);
				}
			});
		}
	};
	
	public static Function<ParametricCurve, Touchable> connectionTouchableMaker = new Function<ParametricCurve, Touchable>(){
		@Override
		public Touchable apply(final ParametricCurve curve) {
			return new Touchable() {
				@Override
				public boolean hitTest(Point2D point) {
					return curve.getPointOnCurve(curve.getNearestPointT(point)).distance(point) < VisualConnection.HIT_THRESHOLD;
				}
				@Override
				public Point2D getCenter()
				{
					return curve.getPointOnCurve(0.5);
				}
				
				@Override
				public Rectangle2D getBoundingBox() {
					return curve.getBoundingBox();
				}
			};
		}
	};
	
	public static Function3<PartialCurveInfo, VisualConnectionProperties, Shape, ColorisableGraphicalContent> connectionGraphicalContentMaker = new Function3<PartialCurveInfo, VisualConnectionProperties, Shape, ColorisableGraphicalContent>(){
		@Override
		public ColorisableGraphicalContent apply(final PartialCurveInfo cInfo, final VisualConnectionProperties connProps, final Shape connectionShape) {
			return new ColorisableGraphicalContent() {
				@Override
				public void draw(DrawRequest r) {
					Graphics2D g = r.getGraphics();
					
					Color color = Coloriser.colorise(connProps.getDrawColor(), r.getColorisation().getColorisation());
					g.setColor(color);
					g.setStroke(connProps.getStroke());
					g.draw(connectionShape);
					
					if (connProps.hasArrow())
						DrawHelper.drawArrowHead(g, color, cInfo.arrowHeadPosition, cInfo.arrowOrientation, 
								connProps.getArrowLength(), connProps.getArrowWidth());
				}
			};
		}
	};
	
	static Function2<VisualConnectionProperties, ParametricCurve, PartialCurveInfo> curveInfoMaker = new Function2<VisualConnectionProperties, ParametricCurve, PartialCurveInfo>() {
		@Override
		public PartialCurveInfo apply(VisualConnectionProperties connectionProperties, ParametricCurve curve) {
			return Geometry.buildConnectionCurveInfo(connectionProperties, curve, 0);
		}
	};
	
	public static ConnectionGui getConnectionGui(final Function<? super Node, ? extends Expression<? extends Touchable>> tp, final VisualConnection connection){
		final Expression<VisualConnectionProperties> properties = getConnectionProperties(tp, connection);
		final Expression<? extends ParametricCurve> curveExpr = bind(connection.graphic(), constant(properties), curveMaker);
		final Expression<? extends PartialCurveInfo> curveInfo = bindFunc(properties, curveExpr, curveInfoMaker);
		final Expression<? extends Shape> connectionPathExpr = bindFunc(curveInfo, curveExpr,new Function2<PartialCurveInfo, ParametricCurve, Shape>(){
			@Override
			public Shape apply(PartialCurveInfo cInfo, ParametricCurve curve) {
				return curve.getShape(cInfo.tStart, cInfo.tEnd);
			}
		});
		
		final Expression<ColorisableGraphicalContent> graphicalContent = bindFunc(curveInfo, properties, connectionPathExpr, VisualConnectionGui.connectionGraphicalContentMaker);
		final Expression<Touchable> touchable = bindFunc(curveExpr, VisualConnectionGui.connectionTouchableMaker);
		return new ConnectionGui(){
			@Override
			public Expression<? extends Touchable> shape() {
				return touchable;
			}
			@Override
			public Expression<? extends ColorisableGraphicalContent> graphicalContent() {
				return graphicalContent;
			}
			@Override
			public Expression<? extends ParametricCurve> parametricCurve() {
				return curveExpr;
			}
		};
	}

	static VisualConnectionGraphicalPropertiesImpl getConnectionProperties(final Function<? super Node, ? extends Expression<? extends Touchable>> tp, final VisualConnection connection) {
		return new VisualConnectionGraphicalPropertiesImpl(tp.apply(connection.getFirst()), tp.apply(connection.getSecond()), connection);
	}
}
