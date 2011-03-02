package org.workcraft.dependencymanager.advanced.core;

public interface Either<T1, T2> {
	public <R> R accept(EitherVisitor<T1, T2, R> visitor);
}
