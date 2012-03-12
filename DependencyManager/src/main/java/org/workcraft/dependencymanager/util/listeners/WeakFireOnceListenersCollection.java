package org.workcraft.dependencymanager.util.listeners;

import org.workcraft.dependencymanager.collections.WeakHashSet;
import org.workcraft.util.Action;


public class WeakFireOnceListenersCollection implements Listener {

	WeakHashSet<Listener> listeners = new WeakHashSet<Listener>();

	@Override
	public Action changed() {
		WeakHashSet<Listener> l = listeners;
		listeners = new WeakHashSet<Listener>();
		Action result = Action.EMPTY;
		for(Listener listener : l) {
			result = Action.Util.combine(result, listener.changed());
		}
		return result;
	}
	
	public void addListener(Listener l) {
		if(l!=null)
			listeners.add(l);
	}
}
