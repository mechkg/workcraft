package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionBase;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class VisualConnectionGui {
	public Expression<? extends GraphicalContent> getGraphics(VisualConnection connection) {
		transformedShape1 = ComponentsTransformer.transform(connection.getFirst(), this);
		transformedShape2 = ComponentsTransformer.transform(connection.getSecond(), this);
		
		
	}
	

	@NoAutoSerialisation
	public ConnectionType getConnectionType() {
		return connectionType;
	}

	@NoAutoSerialisation
	public void setConnectionType(ConnectionType t) {
		if (connectionType!=t) {
			children.remove(graphic);
			
			if (t==ConnectionType.POLYLINE) { 
				graphic = new Polyline(this, storage);
			}
			if (t==ConnectionType.BEZIER) { 
				Bezier b = new Bezier(this, storage);
				b.setDefaultControlPoints();
				graphic = b;
			}
			
			children.add(graphic);
			
			connectionType = t;
		}
	}


	public Point2D getPointOnConnection(double t) {
		return GlobalCache.eval(graphic.curve()).getPointOnCurve(t);
	}

	public Point2D getNearestPointOnConnection(Point2D pt) {
		return GlobalCache.eval(graphic.curve()).getNearestPointOnCurve(pt);
	}
	private ModifiableExpression<ConnectionType> connectionTypeExpr = new ModifiableExpressionBase<ConnectionType>(){
		@Override
		public void setValue(ConnectionType newValue) {
			setConnectionType(newValue);
		}

		@Override
		protected ConnectionType evaluate(EvaluationContext context) {
			return getConnectionType();
		}
	};
	
}
