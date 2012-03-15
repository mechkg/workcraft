package org.workcraft.gui.workspace;


public interface WorkspaceFilter<T> {
	T interpret(Path<String> path);
}
