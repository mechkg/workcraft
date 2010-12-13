package org.workcraft.relational.petrinet.model;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.relational.engine.DatabaseEngine;
import org.workcraft.relational.engine.Id;

public class DatabaseUtils {
	public static ModifiableExpression<Object> fieldValue(final DatabaseEngine engine, final String object, final String fieldName, final Id id) {
		return new ModifiableExpressionImpl<Object>() {

			@Override
			protected void simpleSetValue(Object newValue) {
				engine.setValue(object, fieldName, id, newValue);
			}

			@Override
			protected Object evaluate(EvaluationContext context) {
				return context.resolve(engine.database()).get(object, fieldName, id);
			}
		};
	}
	
	public static <T> ModifiableExpression<T> fieldValue(final DatabaseEngine engine, final String object, final String fieldName, final Class<T> type, final Id id) {
		return new ModifiableExpressionImpl<T>() {

			@Override
			protected void simpleSetValue(T newValue) {
				engine.setValue(object, fieldName, id, newValue);
			}

			@Override
			protected T evaluate(EvaluationContext context) {
				return type.cast(context.resolve(engine.database()).get(object, fieldName, id));
			}
		};
	}
}
