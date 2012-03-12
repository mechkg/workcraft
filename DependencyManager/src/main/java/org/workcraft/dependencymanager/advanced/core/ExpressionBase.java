package org.workcraft.dependencymanager.advanced.core;

import java.util.ArrayList;

import org.workcraft.dependencymanager.util.listeners.Listener;
import org.workcraft.dependencymanager.util.listeners.WeakFireOnceListenersCollection;
import org.workcraft.util.Action;


/**
 * The code with which I tried to explain expressions to Ulan:
 * 
 * 
 * type Resolved = IO -- or maybe something better
 * 
 * type EvaluationContext = forall a. Expression a -> Resolved a
 * 
 * type ExpressionMaker a = EvaluationContext -> Resolved a 
 * 
 * mkExpressionBase :: ExpressionMaker a -> IO (Expression a)
 * 
 * fmap :: (a -> b) -> (Expression a -> IO (Expression b))
 * fmap f a = mkExpressionBase (\context -> f <$> context a)
 * 
 * join :: Expression a -> IO (Expression a)
 * join a = mkExpressionBase (\context -> context a >>= context)
 * 
 * old_bind  :: Expression a -> (a -> IO (Expression b)) -> IO (Expression b)
 * old_bind a f = mkExpressionBase (\context -> context a >>= f >>= context)
 * 
 * new_bind :: Expression a -> (a -> IO (Expression b)) -> IO (Expression b)
 * new_bind a f = fmap f a >>= join
 */

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
	
	boolean printCreationStackTraces = false;
	Exception creationStackTrace = printCreationStackTraces ? new RuntimeException("Expression creation stack trace") : null;
	
	static class Cache<T> implements Listener, Handle {
		public T value;
		public boolean filled = false;
		public boolean valid() {
			return listeners != null;
		}
		ArrayList<Handle> dependencies = new ArrayList<Handle>(1); // used to make sure the dependencies don't get garbage collected too early
		//Exception stackTrace = new Exception("Stack trace");

		WeakFireOnceListenersCollection listeners = new WeakFireOnceListenersCollection();
		public Exception invalidationStackTrace;
		
		boolean warningGiven = false;
		
		public <T2> T2 getValue(Expression<T2> expr) {
			ValueHandleTuple<? extends T2> res = expr.getValue(this);
			dependencies.add(res.handle);
			if(!warningGiven && dependencies.size()==100){
				warningGiven = true;
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
		public Action changed() {
			if(listeners != null) {
				if(!filled)
					invalidationStackTrace = new RuntimeException("Expression invalidation stack trace");
				WeakFireOnceListenersCollection l = listeners;
				listeners = null;
				return l.changed();
			} else
				return Action.EMPTY;
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
		final T result;
		try{
			result = evaluate(new EvaluationContext() {
			@Override
			public <T2> T2 resolve(Expression<T2> dependency) {
				return c.getValue(dependency);
			}
		});
		}
		catch(RuntimeException t) {
			if(printCreationStackTraces) {
				System.err.println("the failed expression was constructed at:");
				creationStackTrace.printStackTrace();
			}
			throw t;
		}
		c.value = result;
		c.filled = true;
		return c;
	}
	
	public final void refresh() {
		if(cache!=null) {
			Cache<?> c = cache;
			cache = null;
			c.changed().run();
		}
	}
	
	abstract protected T evaluate(EvaluationContext context);
}
