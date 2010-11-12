package org.workcraft.dependencymanager.advanced.core;

public class Expressions {
	public static <T> Expression<T> constant(final T value) {
		return new Expression<T> () {
			@Override
			public T evaluate(EvaluationContext resolver) {
				return value;
			}
		};
	}
}
