package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;

final class VisualConnectionGraphicalPropertiesImpl extends ExpressionBase<VisualConnectionProperties> {
	private final Expression<? extends Touchable> shape1;
	private final Expression<? extends Touchable> shape2;
	private final VisualConnection connection;

	public VisualConnectionGraphicalPropertiesImpl(Expression<? extends Touchable> shape1, Expression<? extends Touchable> shape2, VisualConnection connection) {
		this.shape1 = shape1;
		this.shape2 = shape2;
		this.connection = connection;
	}
	
	@Override
	public VisualConnectionProperties evaluate(final EvaluationContext resolver) {
		final Touchable firstShape = resolver.resolve(shape1);
		final Touchable secondShape = resolver.resolve(shape2);
		return new VisualConnectionProperties() {
			@Override
			public Color getDrawColor() {
				return resolver.resolve(connection.color());
			}

			@Override
			public double getArrowWidth() {
				return resolver.resolve(connection.arrowWidth());
			}

			@Override
			public boolean hasArrow() {
				return true;
			}

			@Override
			public Touchable getFirstShape() {
				return firstShape;
			}

			@Override
			public Touchable getSecondShape() {
				return secondShape;
			}

			@Override
			public Stroke getStroke()
			{
				return new BasicStroke((float)resolver.resolve(connection.lineWidth()).doubleValue());
			}

			@Override
			public double getArrowLength() {
				return resolver.resolve(connection.arrowLength());
			}

			@Override
			public ScaleMode getScaleMode() {
				return resolver.resolve(connection.scaleMode());
			}
		};
	}
}
