package org.workcraft.dependencymanager.advanced.core;

import org.workcraft.dependencymanager.advanced.user.AutoRefreshExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;


public class GlobalCache {
	public static <T> T eval(Expression<T> expression) {
		return expression.getValue(null).value;
	}

	public static <T> void assign(ModifiableExpression<T> destination, Expression<T> source) {
		destination.setValue(eval(source));
	}

	public static AutoRefreshExpression autoRefresh(final ExpressionBase<?> expr) {
		
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
