package org.workcraft.plugins.cpog;

import static org.workcraft.plugins.cpog.Component.Util.*;
import static org.workcraft.util.Maybe.Util.*;

import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.tools.SafeConnectionManager;
import org.workcraft.util.Action;
import org.workcraft.util.Maybe.Util.NothingFound;

public class CpogConnectionManager implements SafeConnectionManager<Component> {

	public CpogConnectionManager(CPOG cpog) {
		this.cpog = cpog;
	}

	CPOG cpog;
	
	@Override
	public Action connect(Component first, Component second) throws InvalidConnectionException
	{
		if (first == second) throw new InvalidConnectionException("Self loops are not allowed");
		
		try {
			final Vertex firstVertex = extract(asVertex(first));
			final Vertex secondVertex = extract(asVertex(second));
		
			return new Action() {
				@Override
				public void run() {
					cpog.connect(firstVertex, secondVertex);
				}
			};
		}
		catch(NothingFound nothing) {
			throw new InvalidConnectionException("Invalid connection: only connections between vertices are allowed");
		}
	}
}
