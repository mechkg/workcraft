package org.workcraft.dependencymanager.advanced.core;

import org.workcraft.dependencymanager.util.listeners.Listener;

public interface EvaluationContext extends Listener {
	<T> T resolve(Expression<T> dependency);
}
