package org.workcraft.dependencymanager.advanced.core;

public interface PickyModifiableExpressionCombinator<T1, T2, S> {
	Expression<? extends T2> get(T1 arg);
	Either<S, T1> set(T2 newVal);	
}
