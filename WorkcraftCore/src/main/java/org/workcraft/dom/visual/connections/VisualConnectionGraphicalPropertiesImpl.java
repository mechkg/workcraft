package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;

private final class VisualConnectionGraphicalPropertiesImpl extends ExpressionBase<VisualConnectionProperties> {
	@Override
	public VisualConnectionProperties evaluate(final EvaluationContext resolver) {
		final Touchable firstShape = resolver.resolve(transformedShape1);
		final Touchable secondShape = resolver.resolve(transformedShape2);
		return new VisualConnectionProperties() {

			@Override
			public Color getDrawColor() {
				return resolver.resolve(color());
			}

			@Override
			public double getArrowWidth() {
				return resolver.resolve(arrowWidth());
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
				return new BasicStroke((float)resolver.resolve(lineWidth()).doubleValue());
			}

			@Override
			public double getArrowLength() {
				return resolver.resolve(arrowLength());
			}

			@Override
			public ScaleMode getScaleMode() {
				return resolver.resolve(scaleMode());
			}
		};
	}
}
