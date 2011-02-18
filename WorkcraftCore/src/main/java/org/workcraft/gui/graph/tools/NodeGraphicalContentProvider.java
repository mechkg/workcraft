package org.workcraft.gui.graph.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.GraphicalContent;

public interface NodeGraphicalContentProvider {
	GraphicalContent getGraphicalContent(Node node);
}
