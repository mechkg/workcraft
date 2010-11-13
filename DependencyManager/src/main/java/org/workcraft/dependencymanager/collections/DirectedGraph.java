package org.workcraft.dependencymanager.collections;

import java.util.Set;

public class DirectedGraph<T> {
	
	public OneToMany<T, T> forward = new OneToMany<T, T>();
	public OneToMany<T, T> backward = new OneToMany<T, T>();
	
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

	public void remove(T victim) {
		Set<T> fwd = forward.get(victim);
		Set<T> bwd = backward.get(victim);
		for(T t : fwd)
			backward.remove(t, victim);
		for(T t : bwd)
			forward.remove(t, victim);
		backward.remove(victim);
		forward.remove(victim);
	}

	public int size() {
		int size1 = backward.size();
		int size2 = forward.size();
		if(size1!=size2)
			throw new RuntimeException("internal error - size desync :(");
		return size1;
	}
}
