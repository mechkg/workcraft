package org.workcraft.dom;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class DefaultHierarchyController implements HierarchyController {
	@Override
	public void add(Container parent, Node node) {
		parent.add(node);
	}

	@Override
	public void remove(Node node) {
		Node parent = eval(node.parent());
		if(parent instanceof Container)
		{
			Container parentContainer = (Container)parent;
			parentContainer.remove(node);
		}
		else 
			// dangerous?
			;
	}
}
