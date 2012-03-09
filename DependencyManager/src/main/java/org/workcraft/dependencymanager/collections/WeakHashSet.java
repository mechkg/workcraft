package org.workcraft.dependencymanager.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

public class WeakHashSet<T> implements Set<T> {

	Object dummy = new Object();
	
	WeakHashMap<T,Object> map = new WeakHashMap<T, Object>(1, 0.75f); 
	
	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public Iterator<T> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public Object[] toArray() {
		return map.keySet().toArray();
	}

	@Override
	public <T2> T2[] toArray(T2[] a) {
		return map.keySet().toArray(a);
	}

	@Override
	public boolean add(T e) {
		return map.put(e, dummy) == null;
	}

	@Override
	public boolean remove(Object o) {
		return map.remove(o)!=null;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return map.keySet().containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean result = false;
		for(T t : c)
			if(add(t)) result = true;
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return map.keySet().retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return map.keySet().removeAll(c);
	}

	@Override
	public void clear() {
		map.clear();
	}

}
