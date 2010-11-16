package org.workcraft.dependencymanager.advanced.core;


public interface EvaluationContext {
	<T> T resolve(Expression<T> dependency);
}
