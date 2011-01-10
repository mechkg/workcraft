package org.workcraft.dependencymanager.advanced.core;

import java.util.HashSet;

import org.workcraft.dependencymanager.util.listeners.Listener;
import org.workcraft.dependencymanager.util.listeners.WeakFireOnceListenersCollection;

public abstract class ExpressionBase<T> implements Expression<T> {
	
	public static class ValueHandleTuple<T> {
		public final T value;
		public final Handle handle;
		public ValueHandleTuple(T value, Handle handle) {
			this.handle = handle;
			this.value = value;
		}
		
		public static <T> ValueHandleTuple<T> create(T value, Handle handle) {
			return new ValueHandleTuple<T>(value, handle);
		}
	}
	
	static class Cache<T> implements Listener, Handle {
		public T value;
		public boolean filled = false;
		public boolean valid() {
			return listeners != null;
		}
		HashSet<Handle> dependencies = new HashSet<Handle>(); // used to make sure the dependencies don't get garbage collected too early
		//Exception stackTrace = new Exception("Stack trace");

		WeakFireOnceListenersCollection listeners = new WeakFireOnceListenersCollection();
		public Exception invalidationStackTrace;
		
		public <T2> T2 getValue(Expression<T2> expr) {
			ValueHandleTuple<T2> res = expr.getValue(this);
			dependencies.add(res.handle);
			if(dependencies.size()==100){
				System.err.println("Warning: dependencies size has became 100!");
				Thread.dumpStack();
			}
			return res.value;
		}
		
/*		@Override
		public String toString() {
			StringWriter writer = new StringWriter();
			stackTrace.printStackTrace(new PrintWriter(writer));
			return super.toString() + " created with the following stack trace: \n" + writer.toString(); 
		}*/
		
		@Override
		public void changed() {
			if(listeners != null) {
				if(!filled)
					invalidationStackTrace = new RuntimeException("Expression invalidation stack trace");
				WeakFireOnceListenersCollection l = listeners;
				listeners = null;
				l.changed();
			}
		}
	}
	
	Cache<T> cache;
	
	public final ValueHandleTuple<T> getValue(Listener subscriber) {
		if(cache != null && cache.valid()) {
			cache.listeners.addListener(subscriber);
			return new ValueHandleTuple<T>(cache.value, cache);
		}
		else {
			Cache<T> c;
			
			do {
				c = makeCache();
				if(!c.valid()) {
					System.err.println("Warning: expression '" + this + "' got self-modified with the following stack trace: ");
					c.invalidationStackTrace.printStackTrace();
					System.err.println("Current stack trace:");
					Thread.dumpStack();
					System.err.println("Re-trying the evaluation...");
				}

			} while (!c.valid());
			
			c.listeners.addListener(subscriber);
			cache = c;
			
			return new ValueHandleTuple<T>(cache.value, cache);
		}
	}

	private Cache<T> makeCache() {
		final Cache<T> c = new Cache<T>();
		T result = evaluate(new EvaluationContext() {
			@Override
			public <T2> T2 resolve(Expression<T2> dependency) {
				return c.getValue(dependency);
			}
		});
		c.value = result;
		c.filled = true;
		return c;
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
