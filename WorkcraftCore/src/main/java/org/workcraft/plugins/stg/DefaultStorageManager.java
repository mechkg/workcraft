package org.workcraft.plugins.stg;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dependencymanager.advanced.user.Variable;

public class DefaultStorageManager implements StorageManager {

	@Override
	public <T> ModifiableExpression<T> create(T initialValue) {
		return Variable.create(initialValue);
	}

}
