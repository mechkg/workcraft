package org.workcraft.gui.graph.tools;

import org.workcraft.exceptions.InvalidConnectionException;

public interface ConnectionManager<T> {
	class Util {
		public static <T> ConnectionManager<T> fromSafe(final SafeConnectionManager<T> safe) {
			return new ConnectionManager<T>() {
				@Override
				public void validateConnection(T node1, T node2) throws InvalidConnectionException {
					safe.connect(node1, node2);
				}

				@Override
				public void connect(T node1, T node2) throws InvalidConnectionException {
					safe.connect(node1, node2).run();
				}
			};
		}
	}
	public void validateConnection(T node1, T node2) throws InvalidConnectionException;
	public void connect(T node1, T node2) throws InvalidConnectionException;
}
