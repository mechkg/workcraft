package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.DependencyResolver;

public abstract class ModifiableExpressionImpl<T> extends MutableExpression<T> implements ModifiableExpression<T> {

	@Override
	public final void setValue(DependencyResolver resolver, T newValue) {
		simpleSetValue(resolver, newValue);
		changed();
	}
	
	protected abstract void simpleSetValue(DependencyResolver resolver, T newValue);
}
