package org.workcraft.dependencymanager.advanced.core;

public final class Handle<T> {
	Handle(Expression<T> expression) {
		this.expression = expression;
	}
	public final Expression<T> expression;
}
