package org.workcraft.dependencymanager.advanced.core;

public interface EitherVisitor<T1, T2, R> {
	public R visit1(T1 value);
	public R visit2(T2 value);
}
