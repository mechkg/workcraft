package org.workcraft.gui.graph.tools;

import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.util.Action;

public interface SafeConnectionManager<T> {
	public Action connect(T node1, T node2) throws InvalidConnectionException;
}
