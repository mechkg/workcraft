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
	
	public static ExpressionBase<Integer> sum(final ExpressionBase<Integer> a, final ExpressionBase<Integer> b) {
		return new ExpressionBase<Integer>() {
			@Override
			public Integer evaluate(EvaluationContext resolver) {
				return resolver.resolve(a) + resolver.resolve(b);
			}

		};
	}
	public static ExpressionBase<Integer> size(final ExpressionBase<? extends Collection<?>> collection) {
		return new ExpressionBase<Integer>() {
			@Override
			public Integer evaluate(EvaluationContext resolver) {
				return resolver.resolve(collection).size();
			}
		};
	}
	
	public static <T> ExpressionBase<T> unfold(Expression<? extends Expression<? extends T>> indirect) {
		return new Unfold<T>(indirect);
	}
}
