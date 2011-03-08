package org.workcraft.util;

public interface MaybeVisitor<T, R> {
	R visitJust(T just);
	R visitNothing();
}
