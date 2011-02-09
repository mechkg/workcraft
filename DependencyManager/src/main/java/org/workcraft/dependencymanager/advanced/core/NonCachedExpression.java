package org.workcraft.dependencymanager.advanced.core;

import java.util.HashSet;

import org.workcraft.dependencymanager.advanced.core.ExpressionBase.ValueHandleTuple;
import org.workcraft.dependencymanager.util.listeners.Listener;

public abstract class NonCachedExpression<T> implements Expression<T> {

	class MultiHandle extends HashSet<Handle> implements Handle {
		private static final long serialVersionUID = 1L;
	}
	
	@Override
	public ValueHandleTuple<T> getValue(final Listener subscriber) {
		
		
		final MultiHandle handle = new MultiHandle();
		return ValueHandleTuple.create(evaluate(new EvaluationContext(){

			@Override
			public <T2> T2 resolve(Expression<T2> dependency) {
				ValueHandleTuple<? extends T2> valueHandle = dependency.getValue(subscriber);
				handle.add(valueHandle.handle);
				return valueHandle.value;
			}
			
		}), handle);
	}
	
	abstract T evaluate(EvaluationContext context);
}
