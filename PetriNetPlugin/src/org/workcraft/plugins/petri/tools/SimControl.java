package org.workcraft.plugins.petri.tools;

public interface SimControl<Event> {
	boolean canFire(Event event);
	void fire(Event event);
	Event getNextEvent();
	void unfire();
}
