package org.workcraft.dependencymanager.advanced.user;



public abstract class ModifiableExpressionImpl<T> extends ModifiableExpressionBase<T> {

	public final void setValue(T newValue) {
		simpleSetValue(newValue);
		refresh();
	}
	
	protected abstract void simpleSetValue(T newValue);
}
