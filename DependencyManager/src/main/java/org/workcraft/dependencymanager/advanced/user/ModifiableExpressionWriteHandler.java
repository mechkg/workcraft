package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.ExpressionBase.ValueHandleTuple;
import org.workcraft.dependencymanager.util.listeners.Listener;

public abstract class ModifiableExpressionWriteHandler<T> implements ModifiableExpression<T> {

	private final ModifiableExpression<T> expr;

	public ModifiableExpressionWriteHandler(ModifiableExpression<T> expr) {
		this.expr = expr;
	}

	@Override
	public ValueHandleTuple<? extends T> getValue(Listener subscriber) {
		return expr.getValue(subscriber);
	}

	@Override
	public void setValue(T newValue) {
		beforeSet(newValue);
		expr.setValue(newValue);
		afterSet(newValue);
	}

	protected void beforeSet(T newValue){}
	protected void afterSet(T newValue){}
}
