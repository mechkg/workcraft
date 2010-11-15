package org.workcraft.dependencymanager.advanced.core;

import org.workcraft.dependencymanager.advanced.core.Expression.ValueListenerTuple;
import org.workcraft.dependencymanager.util.listeners.Listener;

public interface IExpression<T> {
	public ValueListenerTuple<T> getValue(Listener subscriber);

}
