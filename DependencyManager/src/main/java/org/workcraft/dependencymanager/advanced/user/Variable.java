package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;



public class Variable<T> extends Expression<T> implements ModifiableExpression<T> {

	private T value;
	
	public Variable(T value) {
		this.value = value;
	}
	
	public void setValue(T value) {
		this.value = value;
		refresh();
	}
	
	public T getValue() {
		return value;
	}
	
	@Override
	protected T evaluate(EvaluationContext context) {
		return value;
	}
	
	@Override
	public String toString() {
		return value == null ? "var equal to  null" : "var of type " + value.getClass() + " = " + value.toString();
	}
}
