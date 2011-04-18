package org.workcraft.dom.visual.connections;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.gui.Coloriser;
import org.workcraft.util.Function;
import org.workcraft.util.Function3;
import org.workcraft.util.Geometry;

public class VisualConnectionGui {

	public interface ExprConnectionGui {
		Expression<? extends Touchable> shape();
		Expression<? extends ColorisableGraphicalContent> graphicalContent();
		Expression<? extends ParametricCurve> parametricCurve();
	}

	public static Function<ParametricCurve, Touchable> connectionTouchableMaker = new Function<ParametricCurve, Touchable>(){
		@Override
		public Touchable apply(final ParametricCurve curve) {
			return makeConnectionTouchable(curve);
		}
	};

	public static Touchable makeConnectionTouchable(final ParametricCurve curve) {
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
	
	public static Function3<PartialCurveInfo, VisualConnectionProperties, Shape, ColorisableGraphicalContent> connectionGraphicalContentMaker = new Function3<PartialCurveInfo, VisualConnectionProperties, Shape, ColorisableGraphicalContent>(){
		@Override
		public ColorisableGraphicalContent apply(final PartialCurveInfo cInfo, final VisualConnectionProperties connProps, final Shape connectionShape) {
			return makeGraphicalContent(cInfo, connProps, connectionShape);
		}
	};

	public static ColorisableGraphicalContent makeGraphicalContent(final PartialCurveInfo cInfo, final VisualConnectionProperties connProps, final Shape connectionShape) {
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
	
	public static ConnectionGui getConnectionGui(final VisualConnectionProperties properties, final VisualConnectionContext context,  VisualConnectionData data) {
		final ParametricCurve curve = data.accept(new ConnectionDataVisitor<ParametricCurve>() {
			@Override
			public ParametricCurve visitPolyline(PolylineData polyline) {
				return PolylineGui.makeCurve(properties, context, polyline.controlPoints());
			}

			@Override
			public ParametricCurve visitBezier(BezierData bezier) {
				return BezierGui.makeCurve(properties, context, bezier.cp1(), bezier.cp2());
			}
		});
		PartialCurveInfo curveInfo = Geometry.buildConnectionCurveInfo(properties, context, curve, 0);
		Shape visiblePath = curve.getShape(curveInfo.tStart, curveInfo.tEnd);
		final ColorisableGraphicalContent gc =makeGraphicalContent(curveInfo, properties, visiblePath);
		final Touchable touchable = makeConnectionTouchable(curve);
		
		return new ConnectionGui() {
			@Override
			public Touchable shape() {
				return touchable;
			}
			
			@Override
			public ParametricCurve parametricCurve() {
				return curve;
			}
			
			@Override
			public ColorisableGraphicalContent graphicalContent() {
				return gc;
			}
		};
	}

	static <N> VisualConnectionGraphicalPropertiesImpl getConnectionProperties(final VisualConnection<N> connection) {
		return new VisualConnectionGraphicalPropertiesImpl(connection);
	}
}
