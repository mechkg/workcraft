package org.workcraft.dependencymanager.advanced.core;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.workcraft.dependencymanager.collections.DirectedGraph;
import org.workcraft.dependencymanager.collections.OneToMany;
import org.workcraft.dependencymanager.util.listeners.Listener;


public class CacheManager {
	
    private static class Weak extends WeakReference<Expression<?>> {
        Weak(Expression<?> value, ReferenceQueue<Expression<?>> queue) {
            super(value, queue);
        }
    }
    
    WeakHashMap<Expression<?>, Weak> nodes = new WeakHashMap<Expression<?>, Weak>(); // :(
    
    ReferenceQueue<Expression<?>> queue = new ReferenceQueue<Expression<?>>(); 
	
	Map<Weak, Object> cache = new HashMap<Weak, Object>();
	DirectedGraph<Weak> dependencies = new DirectedGraph<Weak>();
	
	public void changed(Expression<?> ex) {
		changed(weaken(ex));
	}
	
	public void changed(Weak weak) {
		cache.remove(weak);
		
		dependencies.removeOutgoingArcs(weak);
		for(Weak weakDependant : new ArrayList<Weak>(dependencies.getIncoming(weak))) {
			changed(weakDependant);
		}
		Listener listener = invalidationListeners.get(weak.get());
		if(listener != null)
			listener.changed();
	}
	
	static int ccc=0;
	private Weak weaken(Expression<?> ex) {
		//System.gc();
		while(true)
		{
			Reference<? extends Expression<?>> deadRef = queue.poll();
			if(deadRef == null)
				break;
			finishRef(deadRef);
		}
		
		Weak weak = nodes.get(ex);
		if(weak==null) {  
			weak = new Weak(ex, queue);
			nodes.put(ex, weak);
		}
		return weak;
	}

	private void finishRef(Reference<? extends Expression<?>> deadRef) {
		Weak weak = (Weak)deadRef;
		cache.remove(weak);
		Set<Weak> incoming = dependencies.getIncoming(weak);
		Set<Weak> outgoing = dependencies.getOutgoing(weak);
		checkCirc(weak);
		if(incoming.isEmpty() || outgoing.isEmpty()) {
			dependencies.remove(weak);
			ensureNoLeaks(incoming);
			ensureNoLeaks(outgoing);
		}
	}
	
	void checkCirc(Weak root) {
		if(false){
		if((ccc++)%500 == 0) makeReport();
		checkCirc(dependencies.forward, root);
		checkCirc(dependencies.backward, root);
		}
	}

	private void makeReport() {
		final Map<Class<?>, Integer> data = new HashMap<Class<?>, Integer>();
		System.out.println("weaks: " + nodes.size() + "; cache: " + cache.size() + "; dependencies: " + dependencies.size() + "; invList: "+ invalidationListeners.size());
		System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();
		System.out.println("weaks: " + nodes.size() + "; cache: " + cache.size() + "; dependencies: " + dependencies.size() + "; invList: "+ invalidationListeners.size());
		for(Weak weak : nodes.values()) {
			Expression<?> strong = weak.get();
			Class<?> c = Object.class;
			if(strong!=null)
				c = strong.getClass();
			if(!data.containsKey(c))
				data.put(c, 1);
			else
				data.put(c, data.get(c)+1);
		}
		ArrayList<Class<?>> toSort = new ArrayList<Class<?>>(data.keySet()); 
		Collections.sort(toSort, new Comparator(){

			@Override
			public int compare(Object o1, Object o2) {
				return data.get(o2)-data.get(o1);
			}});
		
		for(int i=0;i<Math.min(50, toSort.size());i++)
			System.out.println(data.get(toSort.get(i)) + " - " + toSort.get(i));
	}

	private void checkCirc(OneToMany<Weak, Weak> graph, Weak root) {
		String result = circular(graph, root);
		if(result != null)
			throw new RuntimeException("Circular!\n"+result);
	}

	private static String circular(OneToMany<Weak, Weak> graph, Weak root) {
		Map<Weak, Weak> all = new HashMap<Weak, Weak>();
		Map<Weak, Weak> todo = new HashMap<Weak, Weak>();
		todo.put(root, null);
		while(!todo.isEmpty())
		{
			Map<Weak, Weak> doing = todo; 
			todo = new HashMap<Weak, Weak>();
			for(Weak weak : doing.keySet())
			{
				if(all.containsKey(weak))
					if(weak == root)
						return backtrack(all, weak);
					else
						continue;
				all.put(weak, doing.get(weak));
				
				for(Weak child : graph.get(weak))
					todo.put(child, weak);
			}
		}
		return null;
	}

	private static String backtrack(Map<Weak, Weak> all, Weak weak) {
		StringBuilder result = new StringBuilder();
		while(weak != null) {
			result.append(weak + ": " + weak.get() + "\n");
			weak = all.get(weak);
		}
		return result.toString();
	}

	private void ensureNoLeaks(Set<Weak> list) {
		for(Weak weak : list)
			finishRef(weak);
	}

	public void registerDependency(Expression<?> expr, Expression<?> dep) {
		Weak we = weaken(expr);
		Weak wd = weaken(dep);
		dependencies.add(we, wd);
		checkCirc(we);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T eval(final Expression<T> expression)
	{
		Weak weak = weaken(expression);
		T result = (T) cache.get(weak);
		if(result == null) {
			result = expression.evaluate(new EvaluationContext()
			{

				@Override
				public <TT> TT resolve(Expression<TT> dependency) {
					registerDependency(expression, dependency);
					return eval(dependency);
				}

				@Override
				public void changed() {
					CacheManager.this.changed(expression);
				}
			}
			);
			cache.put(weak, result);
		}
		return result;
	}

	// TODO: remove this huge memory leak
	Map<Expression<?>, Listener> invalidationListeners = new HashMap<Expression<?>, Listener>();
	
	public void onInvalidate(Expression<?> expr, Listener listener) {
		invalidationListeners.put(expr, listener);
	}
}
