package org.workcraft.plugins.petri;

import java.util.Collection;

import org.workcraft.dom.math.MathModel;

public interface PetriNetModel extends MathModel {
	public Collection<? extends Transition> getTransitions();
	public Collection<? extends Place> getPlaces();
	
	public Place createPlace(String name);
	public Place createPlace();
	
	public Transition createDummyTransition(String name);
	public Transition createTransition();
	
	public boolean isEnabled (Transition t);
	public void fire (Transition t);
	
	public boolean isUnfireEnabled (Transition t);
	public void unFire(Transition t);
}