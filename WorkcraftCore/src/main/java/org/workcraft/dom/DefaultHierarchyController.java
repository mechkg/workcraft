package org.workcraft.dom;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class DefaultHierarchyController implements HierarchyController {
	@Override
	public void add(Container parent, Node node) {
		parent.add(node);
	}

	@Override
	public void remove(Node node) {
		Container parent = (Container)eval(node.parent());
		parent.remove(node);
	}
}
