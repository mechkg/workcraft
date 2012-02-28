package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;

public class Variable<T> extends ExpressionBase<T> implements ModifiableExpression<T> {

	private T value;
	
	public Variable(T value) {
		this.value = value;
	}
	
	@Override
	public void setValue(T value) {
		if(this.value == value)
			return;
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

	public static <T> Variable<T> create(T initialValue) {
		return new Variable<T>(initialValue);
	}
}