package org.workcraft.dependencymanager.collections;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class WeakOneToMany<T1, T2> {
	private Map<T1, Set<T2>> data = new WeakHashMap<T1, Set<T2>>();

	public Set<T2> get(T1 t) {
		Set<T2> result = data.get(t);
		return result !=null ? result : Collections.<T2>emptySet();
	}

	public boolean remove(T1 t1) {
		return data.remove(t1) != null;
	}
	
	public boolean remove(T1 t1, T2 t2) {
		Set<T2> set = data.get(t1);
		if (set == null)
			return false;

		boolean returnValue = set.remove(t2);
		
		if(set.isEmpty())
			data.remove(set);

		return returnValue;
	}

	public void add(T1 t1, T2 t2) {
		Set<T2> set = data.get(t1);
		if(set == null) {
			set = new WeakHashSet<T2>();
			data.put(t1, set);
		}
		set.add(t2);
	}
}
