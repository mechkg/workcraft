package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.IExpression;

public final class Unfold<T> extends Expression<T> {
	private final IExpression<? extends IExpression<? extends T>> indirect;

	public Unfold(IExpression<? extends IExpression<? extends T>> propertyObject) {
		indirect = propertyObject;
	}

	@Override
	protected T evaluate(EvaluationContext context) {
		IExpression<? extends T> expr = context.resolve(indirect);
		if(expr == null)
			return null;
		else
			return context.resolve(expr);
	}
}
