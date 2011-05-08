package org.workcraft.plugins.circuit;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;

public class CircuitConnection extends MathConnection {
	public CircuitConnection(StorageManager storage) {
		super(storage);
	}
	
	public CircuitConnection(MathNode first, MathNode second, StorageManager storage) {
		super(first, second, storage);
	}
}
