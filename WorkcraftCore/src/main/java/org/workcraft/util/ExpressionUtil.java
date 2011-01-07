package org.workcraft.util;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.exceptions.NotSupportedException;

public class ExpressionUtil {
	
	public static <T> ModifiableExpression<T> modificationNotSupported(final Expression<T> expression) {
		return new ModifiableExpressionImpl<T>() {
			@Override
			protected T evaluate(EvaluationContext context) {
				return context.resolve(expression);
			}

			@Override
			public void simpleSetValue(T newValue) {
				throw new NotSupportedException();
			}
		};
	}
}
