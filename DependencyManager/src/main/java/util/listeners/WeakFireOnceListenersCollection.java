package util.listeners;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class WeakFireOnceListenersCollection implements Listener {

	ReferenceQueue<Listener> clearedRefs = new ReferenceQueue<Listener>();
	List<WeakReference<Listener>> listeners = new ArrayList<WeakReference<Listener>>(); 
	
	@Override
	public void changed() {
		clean();
		List<WeakReference<Listener>> l = listeners;
		listeners = new ArrayList<WeakReference<Listener>>();
		for(WeakReference<Listener> ref : l)
		{
			Listener listener = ref.get();
			if(listener != null)
				listener.changed();
				
		}
	}
	
	public void addListener(Listener l) {
		maybeClean();
		listeners.add(new WeakReference<Listener>(l, clearedRefs));
	}

	private void maybeClean() {
		if(isPowerOf2(listeners.size()))
			clean();
	}

	private boolean isPowerOf2(int size) {
		return ((size-1) & size) == 0;
	}

	private void clean() {
		ArrayList<Reference<? extends Listener>> toDelete = new ArrayList<Reference<? extends Listener>>();
		while(true) {
			Reference<? extends Listener> ref = clearedRefs.poll();
			if(ref == null)
				break;
			else
				toDelete.add(ref);
		}
		listeners.removeAll(toDelete);
	}
}
