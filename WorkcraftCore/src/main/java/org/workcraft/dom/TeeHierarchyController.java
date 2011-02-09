package org.workcraft.dom;

import org.workcraft.observation.HierarchyObserver;

import static java.util.Arrays.*;

public class TeeHierarchyController implements HierarchyController {
	private final HierarchyObserver[] observers;
	private final HierarchyController next;

	public TeeHierarchyController(HierarchyController next, HierarchyObserver ... observers) {
		this.next = next;
		this.observers = observers;
	}

	@Override
	public void add(Container parent, Node node) {
		next.add(parent, node);
		for (HierarchyObserver obs : observers)
			obs.handleEvent(asList(node), asList(new Node[]{}));
	}

	@Override
	public void remove(Node node) {
		next.remove(node);
		for (HierarchyObserver obs : observers)
			obs.handleEvent(asList(new Node[]{}), asList(node));
	}
}