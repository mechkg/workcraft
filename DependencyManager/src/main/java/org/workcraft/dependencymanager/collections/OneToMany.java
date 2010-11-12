package org.workcraft.dependencymanager.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OneToMany<T1, T2> {
	private Map<T1, Set<T2>> data = new HashMap<T1, Set<T2>>();

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

	public int size() {
		int result = 0;
		for(Set<T2> s : data.values())
			result += s.size();
		return result;
	}
}
