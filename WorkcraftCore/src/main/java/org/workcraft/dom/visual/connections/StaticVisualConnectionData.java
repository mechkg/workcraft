package org.workcraft.dom.visual.connections;

public interface StaticVisualConnectionData {
	<T> T accept(StaticConnectionDataVisitor<T> visitor);
}
