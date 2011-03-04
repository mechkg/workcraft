package org.workcraft.gui.graph.tools;

import org.workcraft.exceptions.InvalidConnectionException;


public interface ConnectionManager<T> {
	public void validateConnection(T node1, T node2) throws InvalidConnectionException;
	public void connect(T node1, T node2) throws InvalidConnectionException;
}
