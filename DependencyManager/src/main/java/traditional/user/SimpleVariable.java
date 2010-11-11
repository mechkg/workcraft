package traditional.user;

import util.listeners.ListenerCollection;

public class SimpleVariable<T> {
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
}
