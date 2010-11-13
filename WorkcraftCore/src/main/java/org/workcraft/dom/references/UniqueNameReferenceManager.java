package org.workcraft.dom.references;


import java.util.List;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

public class UniqueNameReferenceManager extends HierarchySupervisor implements ReferenceManager 
{
	final private UniqueNameManager<Node> manager;
	private References existing;

	public UniqueNameReferenceManager(Container root, References existing, Func<Node, String> defaultName) {
		this (root, null, existing, defaultName);
	}
	public UniqueNameReferenceManager(Container root, UniqueNameManager<Node> manager, References existing, Func<Node, String> defaultName)
	{
		super(root);
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

	@Override
	public void handleEvent(List<Node> added, List<Node> removed) {
		for(Node node : added) {
			manager.setDefaultNameIfUnnamed(node);
			for (Node node2 : Hierarchy.getDescendantsOfType(node, Node.class))
				manager.setDefaultNameIfUnnamed(node2);
		}
		for(Node node : removed) {
			manager.remove(node);
			for (Node node2 : Hierarchy.getDescendantsOfType(node, Node.class))
				manager.remove(node2);
		}
	}
	
	public void setName(Node node, String label) {
		manager.setName(node, label);
	}
}