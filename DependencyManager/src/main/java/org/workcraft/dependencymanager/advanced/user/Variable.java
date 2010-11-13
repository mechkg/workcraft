package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.DependencyResolver;



public class Variable<T> extends MutableExpression<T> implements ModifiableExpression<T> {

	private T value;
	
	public Variable(T value) {
		this.value = value;
	}
	
	public void setValue(T value) {
		this.value = value;
		changed();
	}
	
	public T getValue() {
		return value;
	}
	
	@Override
	public T simpleEvaluate(DependencyResolver resolver) {
		return value;
	}

	@Override
	public void setValue(DependencyResolver resolver, T newValue) {
		setValue(newValue);
	} 
}
