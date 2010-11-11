package util.listeners;

import java.util.ArrayList;
import java.util.List;


public class ListenerCollection implements Listener {
	
	List<Listener> listeners = new ArrayList<Listener>(0);
	
	public void addListener(Listener l) {
		listeners.add(l);
	}

	public void removeListener(Listener l) {
		listeners.remove(l);
	}

	@Override
	public void changed() {
		for(Listener listener : listeners)
			listener.changed();
	}
}
