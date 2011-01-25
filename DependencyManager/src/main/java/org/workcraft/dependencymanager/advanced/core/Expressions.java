package org.workcraft.dependencymanager.advanced.core;

import java.util.Collection;

import org.workcraft.dependencymanager.advanced.user.Unfold;

public class Expressions {
	public static <T> Expression<T> constant(final T value) {
		return new ExpressionBase<T> () {
			@Override
			public T evaluate(EvaluationContext resolver) {
				return value;
			}
		};
	}
	
	public static Expression<Integer> sum(final Expression<Integer> a, final Expression<Integer> b) {
		return new ExpressionBase<Integer>() {
			@Override
			public Integer evaluate(EvaluationContext resolver) {
				return resolver.resolve(a) + resolver.resolve(b);
			}

		};
	}
	public static Expression<Integer> size(final Expression<? extends Collection<?>> collection) {
		return new ExpressionBase<Integer>() {
			@Override
			public Integer evaluate(EvaluationContext resolver) {
				return resolver.resolve(collection).size();
			}
		};
	}
	
	public static <T> Expression<T> unfold(Expression<? extends Expression<? extends T>> indirect) {
		return new Unfold<T>(indirect);
	}
}
