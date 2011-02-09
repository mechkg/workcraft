package org.workcraft.dom;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.references.ReferenceManager;

public class ModelSpecification {
	public final Container root;
	public final Expression<? extends ReferenceManager> referenceManager;
	public final HierarchyController hierarchyController;
	public final Expression<? extends NodeContext> nodeContext;
	
	public ModelSpecification(Container root, Expression<? extends ReferenceManager> rm, HierarchyController hierarchyController, Expression<? extends NodeContext> nodeContext) {
		this.root = root;
		this.referenceManager = rm;
		this.hierarchyController = hierarchyController;
		this.nodeContext = nodeContext;
	}
}
