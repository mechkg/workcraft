package org.workcraft.gui.graph.tools;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.GraphicalContent;

public interface NodePainter {
	Expression<? extends GraphicalContent> getGraphicalContent(Node node);
}
