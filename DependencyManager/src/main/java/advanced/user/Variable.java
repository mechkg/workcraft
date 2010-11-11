package advanced.user;

import util.listeners.FireOnceListenerCollection;
import advanced.core.DependencyResolver;
import advanced.core.Expression;

public class Variable<T> implements Expression<T> {

	private T value;
	private FireOnceListenerCollection listeners = new FireOnceListenerCollection();
	
	public Variable(T value) {
		this.value = value;
	}
	
	public void setValue(T value) {
		this.value = value;
		listeners.changed();
	}
	
	public T getValue() {
		return value;
	}
	
	@Override
	public T evaluate(final DependencyResolver resolver) {
		listeners.addListener(resolver);
		return value;
	} 
}
