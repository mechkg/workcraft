package org.workcraft.dependencymanager.advanced.core;


public interface DependencyResolver {
	<T> T resolve(IExpression<T> dependency);
}
