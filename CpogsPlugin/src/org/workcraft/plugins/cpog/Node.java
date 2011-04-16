package org.workcraft.plugins.cpog;

public interface Node {
	public <T> T accept(NodeVisitor<T> visitor);
}
