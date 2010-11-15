package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.Expression;


public abstract class ModifiableExpressionImpl<T> extends Expression<T> implements ModifiableExpression<T> {

	public final void setValue(T newValue) {
		simpleSetValue(newValue);
		refresh();
	}
	
	protected abstract void simpleSetValue(T newValue);
}
