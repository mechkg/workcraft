package org.workcraft.dom.visual.connections;

import org.workcraft.dom.Node;

public interface ConnectionGraphicConfiguration extends Node { // TODO: drop "Node"
	public <T> T accept(ConnectionGraphicConfigurationVisitor<T> visitor);
}
