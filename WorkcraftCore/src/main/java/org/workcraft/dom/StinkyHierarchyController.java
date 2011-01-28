package org.workcraft.dom;

import org.workcraft.observation.HierarchyObserver;

public class StinkyHierarchyController implements HierarchyController {
	private final HierarchyObserver[] observers;
	private final HierarchyController next;

	public StinkyHierarchyController(HierarchyController next, HierarchyObserver ... observers) {
		this.next = next;
		this.observers = observers;
	}

	@Override
	public void add(Container parent, Node node) {
		next.add(parent, node);
		for (HierarchyObserver obs : observers)
			obs.nodeAdded(node);
	}

	@Override
	public void remove(Node node) {
		next.remove(node);
		for (HierarchyObserver obs : observers)
			obs.nodeRemoved(node);
	}
}