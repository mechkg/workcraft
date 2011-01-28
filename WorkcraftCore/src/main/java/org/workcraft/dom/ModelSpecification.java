package org.workcraft.dom;

import org.workcraft.dom.references.ReferenceManager;

public class ModelSpecification {
	public final Container root;
	public final ReferenceManager referenceManager;
	public final HierarchyController hierarchyController;
	public final NodeContext nodeContext;
	
	public ModelSpecification(Container root, ReferenceManager referenceManager, HierarchyController hierarchyController, NodeContext nodeContext) {
		this.root = root;
		this.referenceManager = referenceManager;
		this.hierarchyController = hierarchyController;
		this.nodeContext = nodeContext;
	}
}
