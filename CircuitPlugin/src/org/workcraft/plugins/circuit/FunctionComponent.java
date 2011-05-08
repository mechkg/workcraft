package org.workcraft.plugins.circuit;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dependencymanager.advanced.user.StorageManager;

@VisualClass(org.workcraft.plugins.circuit.VisualFunctionComponent.class)

public class FunctionComponent extends CircuitComponent {

	public FunctionComponent(StorageManager storage) {
		super(storage);
	}
	
}
