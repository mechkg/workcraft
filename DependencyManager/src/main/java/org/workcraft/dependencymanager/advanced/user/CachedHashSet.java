package org.workcraft.dependencymanager.advanced.user;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;

public class CachedHashSet<Node> extends ExpressionBase<Set<Node>> {

	private final ModifiableExpression<LinkedHashSet<Node>> data;
	
	public CachedHashSet(StorageManager storage) {
		data = storage.create(new LinkedHashSet<Node>());
	}

	public void add(Node node) {
		LinkedHashSet<Node> newValue = new LinkedHashSet<Node>(getValue());
		newValue.add(node);
		data.setValue(newValue);
	}
	
	public void remove(Node node) {
		LinkedHashSet<Node> newValue = new LinkedHashSet<Node>(getValue());
		if(newValue.remove(node))
			data.setValue(newValue);
	}
	
	public LinkedHashSet<Node> getValue() {
		return GlobalCache.eval(data);
	}
	
	@Override
	public Set<Node> evaluate(EvaluationContext resolver) {
		return resolver.resolve(data);
	}

	public void clear() {
		data.setValue(new LinkedHashSet<Node>());
	}

	public void addAll(Collection<? extends Node> nodes) {
		LinkedHashSet<Node> newValue = new LinkedHashSet<Node>(getValue());
		newValue.addAll(nodes);
		data.setValue(newValue);
	}

	public void removeAll(Collection<Node> nodes) {
		LinkedHashSet<Node> newValue = new LinkedHashSet<Node>(getValue());
		if(newValue.removeAll(nodes))
			data.setValue(newValue);
	}
}
