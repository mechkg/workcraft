package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.IExpression;

public interface ModifiableExpression<T> extends IExpression<T> {
	public abstract void setValue(T newValue);
}
