package org.workcraft.dependencymanager.collections;

import java.util.Set;

public class WeakDirectedGraph<T> {
	
	WeakOneToMany<T, T> forward = new WeakOneToMany<T, T>();
	WeakOneToMany<T, T> backward = new WeakOneToMany<T, T>();
	
	public Set<T> getOutgoing(T t) {
		return forward.get(t);
	}
	
	public Set<T> getIncoming(T t) {
		return backward.get(t);
	}

	public void removeOutgoingArcs(T t) {
		Set<T> dependants = forward.get(t);
		for(T d : dependants)
			backward.remove(d,t);
		forward.remove(t);
	}

	public void add(T expr, T dep) {
		forward.add(expr, dep);
		backward.add(dep, expr);
	}
}
