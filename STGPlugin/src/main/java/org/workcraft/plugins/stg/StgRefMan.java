package org.workcraft.plugins.stg;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.Node;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.Pair;

public interface StgRefMan {
	public void setInstance(DummyTransition dt, Pair<String, Integer> instance);
	public void setInstance(SignalTransition st, Pair<Pair<String, Direction>, Integer> instance);
	public void setName(STGPlace place, String name);
	public void setMiscNodeName(Node node, String s);
	
	public Expression<StgRefManState> state();
}
