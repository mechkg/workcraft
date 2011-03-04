package org.workcraft.dom.visual.connections;

import java.awt.geom.AffineTransform;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;

public class ControlPointGui {
	@Override
	public ModifiableExpression<AffineTransform> transform() {
		Expression<ModifiableExpression<AffineTransform>> expr = new ExpressionBase<ModifiableExpression<AffineTransform>>() {

			@Override
			protected ModifiableExpression<AffineTransform> evaluate(EvaluationContext context) {
				ConnectionGraphicConfiguration parent = (ConnectionGraphicConfiguration)context.resolve(parent());
				if(parent == null)
					return simpleTransform();
				return context.resolve(parent.scaler()).get(ControlPoint.this);
			}
		};
		
		return unfold(expr);
	}

	
	private <T> ModifiableExpression<T> unfold(final Expression<? extends ModifiableExpression<T>> expr) {
		return new ModifiableExpressionImpl<T>() {

			@Override
			protected void simpleSetValue(T newValue) {
				GlobalCache.eval(expr).setValue(newValue);
			}

			@Override
			protected T evaluate(EvaluationContext context) {
				return context.resolve(context.resolve(expr));
			}
		};
	}

}
