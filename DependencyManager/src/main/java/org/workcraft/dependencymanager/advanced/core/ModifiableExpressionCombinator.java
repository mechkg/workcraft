package org.workcraft.dependencymanager.advanced.core;

public interface ModifiableExpressionCombinator<T1, T2> {
	Expression<? extends T2> get(T1 arg);
	T1 set(T2 newVal);
}
