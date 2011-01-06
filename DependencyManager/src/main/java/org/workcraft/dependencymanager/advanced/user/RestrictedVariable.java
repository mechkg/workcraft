package org.workcraft.dependencymanager.advanced.user;

public abstract class RestrictedVariable<T> extends Variable<T> {

	public RestrictedVariable(T initialValue) {
		super(initialValue);
	}
	
	protected abstract T correctValue(T oldValue, T newValue);
}
