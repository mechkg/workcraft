package org.workcraft.dependencymanager.advanced.core;

import org.workcraft.dependencymanager.advanced.user.AutoRefreshExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.util.Function;

public class GlobalCache {
	public static <T> T eval(Expression<? extends T> expression) {
		return expression.getValue(null).value;
	}

	public static <A, B> Function<A, B> eval(final Function<? super A, ? extends Expression<? extends B>> function) {
		return new Function<A, B>() {
			@Override
			public B apply(A arg) {
				return eval(function.apply(arg));
			}
		};
	}

	public static <T> void assign(ModifiableExpression<T> destination,
			Expression<? extends T> source) {
		destination.setValue(eval(source));
	}

	public static AutoRefreshExpression autoRefresh(final Expression<?> expr) {

		return new AutoRefreshExpression() {
			@Override
			protected void onEvaluate(EvaluationContext context) {
				context.resolve(expr);
			}
		};
	}

	public static <T> void setValue(ModifiableExpression<T> expr, T value) {
		expr.setValue(value);
	}
}
