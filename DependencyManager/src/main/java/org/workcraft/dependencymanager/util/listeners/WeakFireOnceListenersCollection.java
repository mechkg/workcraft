package org.workcraft.dependencymanager.util.listeners;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;


public class WeakFireOnceListenersCollection implements Listener {

	ReferenceQueue<Listener> clearedRefs = new ReferenceQueue<Listener>();
	HashSet<WeakReference<Listener>> listeners = new HashSet<WeakReference<Listener>>();

	@Override
	public void changed() {
		clean();
		HashSet<WeakReference<Listener>> l = listeners;
		listeners = new HashSet<WeakReference<Listener>>();
		for(WeakReference<Listener> ref : l)
		{
			Listener listener = ref.get();
			if(listener != null)
				listener.changed();
		}
	}
	
	public void addListener(Listener l) {
		if(l != null) {
			clean();
			listeners.add(new WeakReference<Listener>(l, clearedRefs));
		}
	}

	private void clean() {
		while(true) {
			Reference<? extends Listener> ref = clearedRefs.poll();
			if(ref == null)
				return;
			else
				listeners.remove(ref);
		}
	}
}
