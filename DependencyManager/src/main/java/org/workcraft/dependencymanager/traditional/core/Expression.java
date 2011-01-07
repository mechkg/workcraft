package org.workcraft.dependencymanager.traditional.core;

import org.workcraft.dependencymanager.util.listeners.Listener;

public interface Expression<T> {
	public T getValue();
	public void addListener(Listener l);
	public void removeListener(Listener l);
}
