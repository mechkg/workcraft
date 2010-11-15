package org.workcraft.dependencymanager.advanced.core;

import java.util.HashSet;

import org.workcraft.dependencymanager.util.listeners.Listener;
import org.workcraft.dependencymanager.util.listeners.WeakFireOnceListenersCollection;

public abstract class Expression<T> implements IExpression<T> {
	
	static class Cache<T> implements Listener {
		public T value;
		public boolean valid = true;
		HashSet<Listener> dependencies = new HashSet<Listener>(); // used to make sure the dependencies don't get garbage collected too early

		WeakFireOnceListenersCollection listeners = new WeakFireOnceListenersCollection();
		
		public <T2> T2 getValue(IExpression<T2> expr) {
			ValueListenerTuple<T2> res = expr.getValue(this);
			Listener l = res.handle;
			dependencies.add(l);
			return res.value;
		}
		
		@Override
		public void changed() {
			if(listeners != null) {
				valid = false;
				listeners.changed();
				listeners = null;
			}
		}
	}
	
	static class ValueListenerTuple<T> {
		public final T value;
		public final Listener handle;
		public ValueListenerTuple(T value, Listener handle) {
			this.handle = handle;
			this.value = value;
		}
	}
	
	Cache<T> cache;
	
	public final ValueListenerTuple<T> getValue(Listener subscriber) {
		if(cache != null && cache.valid) {
			cache.listeners.addListener(subscriber);
			return new ValueListenerTuple<T>(cache.value, cache);
		}
		else {
			final Cache<T> c = new Cache<T>();
			T result = evaluate(new EvaluationContext() {
				@Override
				public <T2> T2 resolve(IExpression<T2> dependency) {
					return c.getValue(dependency);
				}
			});
			c.value = result;
			
			cache = c;
			cache.listeners.addListener(subscriber);
			
			return new ValueListenerTuple<T>(result, cache);
		}
	}
	
	public final void refresh() {
		if(cache!=null) {
			Cache<?> c = cache;
			cache = null;
			c.changed();
		}
	}
	
	abstract protected T evaluate(EvaluationContext context);
}
