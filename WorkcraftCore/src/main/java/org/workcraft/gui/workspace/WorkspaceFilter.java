package org.workcraft.gui.workspace;


import checkers.nullness.quals.Nullable;

public interface WorkspaceFilter<T> {
	@Nullable T interpret(Path<String> path);
}
