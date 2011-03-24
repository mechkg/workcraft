package org.workcraft.util;

public interface GenericFunction {
	<A, R> R apply(A arg);
}
