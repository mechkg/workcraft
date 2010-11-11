package util.listeners;

import java.util.ArrayList;
import java.util.List;


public class FireOnceListenerCollection implements Listener {
	List<Listener> listeners = new ArrayList<Listener>(0);
	
	public void addListener(Listener l) {
		listeners.add(l);
	}

	@Override
	public void changed() {
		List<Listener> l = listeners;
		listeners = new ArrayList<Listener>();
				
		for(Listener listener : l)
			listener.changed();
	}
}
