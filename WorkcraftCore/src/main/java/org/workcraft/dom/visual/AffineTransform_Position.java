package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.dom.visual.VisualTransformableNode.AffineTransform_X;
import org.workcraft.dom.visual.VisualTransformableNode.AffineTransform_Y;

public final class AffineTransform_Position extends ModifiableExpressionImpl<Point2D.Double> {
	private final AffineTransform_X x;
	private final AffineTransform_Y y;

	public AffineTransform_Position(ModifiableExpression<AffineTransform> transform) {
		this.x = new AffineTransform_X(transform);
		this.y = new AffineTransform_Y(transform);
	}
	
	@Override
	public Point2D.Double evaluate(EvaluationContext resolver) {
		return new Point2D.Double(resolver.resolve(x), resolver.resolve(y)); 
	}

	@Override
	public void simpleSetValue(Point2D.Double newValue) {
		x.setValue(newValue.getX());
		y.setValue(newValue.getY());
	}
}