package org.workcraft.dependencymanager.advanced.core;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.workcraft.dependencymanager.collections.DirectedGraph;


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
		if(circular(dependencies, weak))
			throw new RuntimeException("circ!");
		if(incoming.isEmpty() || outgoing.isEmpty()) {
			dependencies.remove(weak);
			ensureNoLeaks(incoming);
			ensureNoLeaks(outgoing);
		}
	}

	private static boolean circular(DirectedGraph<Weak> dependencies2, Weak root) {
		Set<Weak> all = new HashSet<Weak>();
		Set<Weak> todo = new HashSet<Weak>();
		todo.add(root);
		while(!todo.isEmpty())
		{
			ArrayList<Weak> cpy = new ArrayList<Weak>(todo);
			todo.clear();
			for(Weak weak : cpy)
			{
				if(all.contains(weak))
					return true;
				all.add(weak);
				todo.addAll(dependencies2.getOutgoing(weak));
			}
		}
		return false;
	}

	private void ensureNoLeaks(Set<Weak> list) {
		for(Weak weak : list)
			finishRef(weak);
	}

	public void registerDependency(Expression<?> expr, Expression<?> dep) {
		dependencies.add(weaken(expr), weaken(dep));
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
}
