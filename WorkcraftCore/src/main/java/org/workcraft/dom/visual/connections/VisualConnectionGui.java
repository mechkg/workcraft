package org.workcraft.dom.visual.connections;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.gui.Coloriser;
import org.workcraft.util.Function;
import org.workcraft.util.Function2;
import org.workcraft.util.Function3;
import org.workcraft.util.Geometry;

public class VisualConnectionGui {

	public static Expression<? extends ColorisableGraphicalContent> getGraphicalContent(final TouchableProvider<Node> tp, final VisualConnection connection, ConnectionGraphicConfiguration connectionGraphic) {
		final VisualConnectionGraphicalPropertiesImpl connectionProperties = new VisualConnectionGraphicalPropertiesImpl(tp.apply(connection.getFirst()), tp.apply(connection.getSecond()), connection);
		return connectionGraphic.accept(new ConnectionGraphicConfigurationVisitor<Expression<? extends ColorisableGraphicalContent>>() {
			@Override
			public Expression<? extends ColorisableGraphicalContent> visitPolyline(PolylineConfiguration polyline) {
				return PolylineGui.getGraphicalContent(connectionProperties, polyline);
			}

			@Override
			public Expression<? extends ColorisableGraphicalContent> visitBezier(BezierConfiguration bezier) {
				return BezierGui.getGraphicalContent(bezier, connectionProperties);
			}
		});
	}
	
	public static Expression<? extends Touchable> getShape(final TouchableProvider<Node> tp, final VisualConnection connection, ConnectionGraphicConfiguration connectionGraphic) {
		final Expression<VisualConnectionProperties> properties = new VisualConnectionGraphicalPropertiesImpl(tp.apply(connection.getFirst()), tp.apply(connection.getSecond()), connection);
		return connectionGraphic.accept(new ConnectionGraphicConfigurationVisitor<Expression<? extends Touchable>>() {
			@Override
			public Expression<? extends Touchable> visitPolyline(PolylineConfiguration polyline) {
				return PolylineGui.shape(polyline, properties);
			}

			@Override
			public Expression<? extends Touchable> visitBezier(BezierConfiguration bezier) {
				return BezierGui.shape(bezier, properties);
			}
			
		});
	}
	
	public static Function<ParametricCurve, Touchable> connectionTouchableMaker = new Function<ParametricCurve, Touchable>(){
		@Override
		public Touchable apply(final ParametricCurve curve) {
			return new Touchable() {
				@Override
				public boolean hitTest(Point2D point) {
					return curve.getDistanceToCurve(point) < VisualConnection.HIT_THRESHOLD;
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
	
	public static Point2D getPointOnConnection(VisualConnection c, Point2D p) {
		
	}
}
