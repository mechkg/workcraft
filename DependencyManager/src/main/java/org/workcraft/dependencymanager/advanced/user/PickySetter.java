package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.Maybe;

public interface PickySetter<T,S> {
	/** haskell type: t -> IO Maybe s
	 * @param newValue
	 * The value to set.
	 * @return
	 * The status of the set operation.
	 */
	public abstract Maybe<S> setValue(T newValue);
}
