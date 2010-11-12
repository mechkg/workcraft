package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.util.listeners.FireOnceListenerCollection;

public abstract class ModifiableExpression<T> implements Expression<T> {

	private FireOnceListenerCollection listeners = new FireOnceListenerCollection();
	
	public ModifiableExpression() {
	}
	
	protected void changed() {
		listeners.changed();
	}
	
	@Override
	public final T evaluate(final EvaluationContext resolver) {
		listeners.addListener(resolver);
		return simpleEvaluate();
	}
	
	public abstract T simpleEvaluate();

}
