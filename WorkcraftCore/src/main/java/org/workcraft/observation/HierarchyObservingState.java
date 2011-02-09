package org.workcraft.observation;

public interface HierarchyObservingState<T> extends HierarchyObserver {
	T getState();
}
