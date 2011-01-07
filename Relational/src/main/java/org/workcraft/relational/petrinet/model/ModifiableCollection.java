package org.workcraft.relational.petrinet.model;

import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.Expression;

public interface ModifiableCollection<T> extends Expression<Collection<T>> {
	void add(T element);
	void remove(T element);
}
