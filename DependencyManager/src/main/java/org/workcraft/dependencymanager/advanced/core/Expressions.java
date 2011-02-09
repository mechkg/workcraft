package org.workcraft.dependencymanager.advanced.core;

import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.ExpressionBase.ValueHandleTuple;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.dependencymanager.advanced.user.Unfold;
import org.workcraft.dependencymanager.util.listeners.Listener;

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

	public static <T2, T1  extends T2>ModifiableExpression<T2> cast(
			final ModifiableExpression<T1> expr,
			final Class<T1> cls) {
		return new ModifiableExpressionImpl<T2>() {

			@Override
			protected void simpleSetValue(T2 newValue) {
				expr.setValue(cls.cast(newValue));
			}

			@Override
			protected T2 evaluate(EvaluationContext context) {
				return context.resolve(expr);
			}
		};
	}

	public static <T> ModifiableExpression<T> modifiableExpression(final Expression<? extends T> getter, final ModifiableExpression<T> setter) {
		return new ModifiableExpression<T>() {

			@Override
			public ValueHandleTuple<? extends T> getValue(Listener subscriber) {
				return getter.getValue(subscriber);
			}

			@Override
			public void setValue(T newValue) {
				setter.setValue(newValue);
			}
		};
	}
}
