package org.workcraft.gui.graph.tools;

import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.util.Action;

public interface ConnectionManager<T> {
	public Action prepareConnection(T node1, T node2) throws InvalidConnectionException; 
}
