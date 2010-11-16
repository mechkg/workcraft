package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expression;

public final class Unfold<T> extends ExpressionBase<T> {
	private final Expression<? extends Expression<? extends T>> indirect;

	public Unfold(Expression<? extends Expression<? extends T>> propertyObject) {
		indirect = propertyObject;
	}

	@Override
	protected T evaluate(EvaluationContext context) {
		Expression<? extends T> expr = context.resolve(indirect);
		if(expr == null)
			return null;
		else
			return context.resolve(expr);
	}
}
