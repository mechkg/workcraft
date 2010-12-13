package org.workcraft.relational.petrinet.modifiable;

import java.util.Map;

import org.workcraft.dependencymanager.advanced.core.Expression;

public interface ModifiableMap<K, V> extends Expression<Map<K,V>> {
	void put(K key, V value);
	void remove(K key);
}
