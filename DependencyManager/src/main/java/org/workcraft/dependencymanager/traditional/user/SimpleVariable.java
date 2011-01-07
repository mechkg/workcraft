package org.workcraft.dependencymanager.traditional.user;

import org.workcraft.dependencymanager.traditional.core.Expression;
import org.workcraft.dependencymanager.util.listeners.Listener;
import org.workcraft.dependencymanager.util.listeners.ListenerCollection;

public class SimpleVariable<T> implements Expression<T> {
	ListenerCollection listeners = new ListenerCollection();
	
	private T value;

	public SimpleVariable(T value) {
		this.setValue(value);
	}

	public void setValue(T value) {
		this.value = value;
		listeners.changed();
	}

	public T getValue() {
		return value;
	}

	public ListenerCollection listeners() {
		return listeners;
	}

	@Override
	public void addListener(Listener l) {
		listeners.addListener(l);
	}

	@Override
	public void removeListener(Listener l) {
		listeners.removeListener(l);
	}
}
