package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;

final class VisualConnectionGraphicalPropertiesImpl extends ExpressionBase<VisualConnectionProperties> {
	private final VisualConnection<?> connection;

	public VisualConnectionGraphicalPropertiesImpl(VisualConnection<?> connection) {
		this.connection = connection;
	}
	
	@Override
	public VisualConnectionProperties evaluate(final EvaluationContext resolver) {
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
			public Stroke getStroke()
			{
				return new BasicStroke((float)resolver.resolve(connection.lineWidth()).doubleValue());
			}

			@Override
			public double getArrowLength() {
				return resolver.resolve(connection.arrowLength());
			}
		};
	}
}
