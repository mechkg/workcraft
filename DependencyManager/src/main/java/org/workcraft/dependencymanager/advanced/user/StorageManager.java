package org.workcraft.dependencymanager.advanced.user;


public interface StorageManager {
	<T> ModifiableExpression<T> create(T initialValue);
}
