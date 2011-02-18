package org.workcraft.plugins.stg;

import org.workcraft.dom.Node;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.Pair;

public interface StgRefManState {
	
	public Pair<String, Integer> getInstance(DummyTransition dt);
	public Pair<Pair<String, Direction>, Integer> getInstance(SignalTransition st);
	public String getName(STGPlace place);
	
	public String getMiscNodeName(Node node);
	
	public DummyTransition getDummyTransition(Pair<String, Integer> instance);
	public SignalTransition getSignalTransition(Pair<Pair<String, Direction>, Integer> instance);
	public STGPlace getPlace(String name);
	public Node getMiscNode(String name);
}
