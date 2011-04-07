package org.workcraft.plugins.petri.tools;

public interface SimulationModel<Event, State> {
	boolean canFire(Event event);
	void fire(Event event);
	boolean canUnfire(Event event);
	void unfire(Event event);
	
	State saveState();
	void loadState(State state);
}
