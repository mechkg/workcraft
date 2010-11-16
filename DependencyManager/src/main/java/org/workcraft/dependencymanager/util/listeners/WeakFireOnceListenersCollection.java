package org.workcraft.dependencymanager.util.listeners;

import org.workcraft.dependencymanager.collections.WeakHashSet;


public class WeakFireOnceListenersCollection implements Listener {

	WeakHashSet<Listener> listeners = new WeakHashSet<Listener>();

	@Override
	public void changed() {
		WeakHashSet<Listener> l = listeners;
		listeners = new WeakHashSet<Listener>();
		for(Listener listener : l)
			listener.changed();
	}
	
	public void addListener(Listener l) {
		if(l!=null)
			listeners.add(l);
	}
}
