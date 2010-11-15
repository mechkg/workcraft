package org.workcraft.dependencymanager.advanced.core;

public interface OldExpression<T> {
	T evaluate(EvaluationContext resolver);
}
