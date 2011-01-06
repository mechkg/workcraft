package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;

public abstract class ModifiableExpressionFilter<ET, IT> extends ModifiableExpressionImpl<ET> {
	private final ModifiableExpression<IT> expr;

	public ModifiableExpressionFilter(ModifiableExpression<IT> expr) {
		this.expr = expr;
	}
	
	protected void simpleSetValue(ET newValue) { expr.setValue(setFilter(newValue)); };
	
	protected abstract IT setFilter(ET newValue);
	protected abstract ET getFilter(IT value);

	@Override
	protected ET evaluate(EvaluationContext context) {
		return getFilter(context.resolve(expr));
	}
}
