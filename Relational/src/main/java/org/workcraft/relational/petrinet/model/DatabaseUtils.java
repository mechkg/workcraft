package org.workcraft.relational.petrinet.model;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.relational.engine.Database;
import org.workcraft.relational.engine.DatabaseEngine;
import org.workcraft.relational.engine.Id;

import pcollections.PVector;
import pcollections.TreePVector;

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
	
	public static <T> ModifiableExpression<T> fieldValue(final DatabaseEngine engine, final String object, final String fieldName, final Class<T> type, Id id) {
		return fieldValue(engine, object, fieldName, type, Expressions.constant(id)); 
	}
		
		public static <T> ModifiableExpression<T> fieldValue(final DatabaseEngine engine, final String object, final String fieldName, final Class<T> type, final Expression<Id> id) {
		return new ModifiableExpressionImpl<T>() {

			@Override
			protected void simpleSetValue(T newValue) {
				engine.setValue(object, fieldName, eval(id), newValue);
			}

			@Override
			protected T evaluate(EvaluationContext context) {
				return type.cast(context.resolve(engine.database()).get(object, fieldName, context.resolve(id)));
			}
		};
	}

		public static Expression<PVector<Id>> children(final DatabaseEngine engine, final String parentObject, final String childObject, final String referenceField, final Expression<Id> parentId) {
			return new ExpressionBase<PVector<Id>>() {

				@Override
				protected PVector<Id> evaluate(EvaluationContext context) {
					Database db = context.resolve(engine.database());
					Collection<Id> candidates = db.list(childObject);
					PVector<Id> result = TreePVector.<Id>empty();
					for(Id candidate : candidates) {
						if(db.get(childObject, referenceField, candidate) == context.resolve(parentId))
							result = result.plus(candidate);
					}
					System.out.println(String.format("%s children for %s.%s->%s with id %s", result.size(), childObject, referenceField, parentObject, context.resolve(parentId)));
					return result;
				}
				
			};
		}
}
