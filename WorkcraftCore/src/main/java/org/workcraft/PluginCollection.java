package org.workcraft;

import org.workcraft.interop.GlobalService;

import pcollections.HashTreePMap;
import pcollections.PMap;
import pcollections.PVector;
import pcollections.TreePVector;

public class PluginCollection {
	private final PMap <GlobalService<?>, PVector<?>> plugins;
	
	private PluginCollection(PMap<GlobalService<?>, PVector<?>> plugins) {
		this.plugins = plugins;
	}
	
	public static PluginCollection EMPTY = new PluginCollection(HashTreePMap.<GlobalService<?>, PVector<?>>empty());

	public <T> PVector<T> getImplementations(GlobalService<T> service) {
		@SuppressWarnings("unchecked")
		final PVector<T> result = (PVector<T>)plugins.get(service);
		if(result != null)
			return result;
		else
			return TreePVector.empty();
	}
	
	public <T> PluginCollection plus(GlobalService<T> service, T implementation) {
		return new PluginCollection(plugins.plus(service, getImplementations(service).plus(implementation)));
	}
}
