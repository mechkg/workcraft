package org.workcraft.dependencymanager.advanced.core;

public interface Expression<T> {
	T evaluate(EvaluationContext resolver);
}
