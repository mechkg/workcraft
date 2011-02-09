package org.workcraft.dom.references;


import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyObservingState;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

public class UniqueNameReferenceManager implements HierarchyObservingState<ReferenceManager>, ReferenceManager
{
	final private UniqueNameManager<Node> manager;
	private References existing;

	public UniqueNameReferenceManager(Node root, References existing, Func<Node, String> defaultName) {
		this (root, null, existing, defaultName);
	}
	public UniqueNameReferenceManager(Node root, UniqueNameManager<Node> manager, References existing, Func<Node, String> defaultName)
	{
		this.existing = existing;
		if (manager == null)
			this.manager = new UniqueNameManager<Node>(defaultName);
		else
			this.manager = manager;

		if (existing != null) {
			setExistingReference(root);
			for(Node n : Hierarchy.getDescendantsOfType(root, Node.class))
				setExistingReference(n);
			existing = null;
		}
	}

	private void setExistingReference(Node n) {
		final String reference = existing.getReference(n);
		if (reference != null)
			manager.setName(n, reference);
	}

	@Override
	public Node getNodeByReference(String reference) {
		return manager.get(reference);
	}

	@Override
	public String getNodeReference(Node node) {
		return manager.getName(node);
	}

	public void setName(Node node, String label) {
		manager.setName(node, label);
	}
	
	private void nodeAdded(Node node) {
		manager.setDefaultNameIfUnnamed(node);
		for (Node node2 : Hierarchy.getDescendantsOfType(node, Node.class))
			manager.setDefaultNameIfUnnamed(node2);
	}
	
	private void nodeRemoved(Node node) {
		manager.remove(node);
		for (Node node2 : Hierarchy.getDescendantsOfType(node, Node.class))
			manager.remove(node2);
	}
	@Override
	public void handleEvent(Collection<? extends Node> added, Collection<? extends Node> removed) {
		for(Node node : removed)
			nodeRemoved(node);
		for(Node node : added)
			nodeAdded(node);
		
	}
	@Override
	public ReferenceManager getState() {
		return this;
	}
}