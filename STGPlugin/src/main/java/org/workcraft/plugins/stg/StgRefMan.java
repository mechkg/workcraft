package org.workcraft.plugins.stg;

import org.workcraft.dom.Node;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.Pair;

public interface StgRefMan {
	public Pair<String, Integer> getInstance(DummyTransition dt);
	public Pair<Pair<String, Direction>, Integer> getInstance(SignalTransition st);
	public String getName(STGPlace place);
	
	public String getMiscNodeName(Node node);
	
	public void setInstance(DummyTransition dt, Pair<String, Integer> instance);
	public void setInstance(SignalTransition st, Pair<Pair<String, Direction>, Integer> instance);
	public void setName(STGPlace place, String name);
	public void setMiscNodeName(Node node, String s);
	
	public DummyTransition getDummyTransition(Pair<String, Integer> instance);
	public SignalTransition getSignalTransition(Pair<Pair<String, Direction>, Integer> instance);
	public STGPlace getPlace(String name);
	public Node getMiscNode(String name);
}
