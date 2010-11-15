package org.workcraft.dependencymanager.advanced.core;


public interface OldDependencyResolver {
	<T> T resolve(Handle<T> dependency);
}
