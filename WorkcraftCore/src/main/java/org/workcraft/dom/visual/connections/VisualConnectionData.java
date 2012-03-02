package org.workcraft.dom.visual.connections;

public interface VisualConnectionData {
	<T> T accept(ConnectionDataVisitor<T> visitor);
}
