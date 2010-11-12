package org.workcraft.dependencymanager.advanced.user;



public class Variable<T> extends ModifiableExpression<T> {

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
	public T simpleEvaluate() {
		return value;
	} 
}
