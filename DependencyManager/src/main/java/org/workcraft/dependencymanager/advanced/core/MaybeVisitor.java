package org.workcraft.dependencymanager.advanced.core;

public interface MaybeVisitor<T, R> {
	R visitJust(T just);
	R visitNothing();
}
