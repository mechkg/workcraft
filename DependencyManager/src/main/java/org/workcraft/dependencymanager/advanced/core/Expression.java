package org.workcraft.dependencymanager.advanced.core;

import org.workcraft.dependencymanager.advanced.core.ExpressionBase.ValueHandleTuple;
import org.workcraft.dependencymanager.util.listeners.Listener;

public interface Expression<T> {
	public ValueHandleTuple<T> getValue(Listener subscriber);
}
