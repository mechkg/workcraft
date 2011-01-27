package org.workcraft;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.plugins.balsa.BalsaCircuit;

public class BalsaModelDescriptor implements ModelDescriptor {
	@Override
	public String getDisplayName() {
		return "Breeze circuit";
	}

	@Override
	public MathModel createMathModel(StorageManager storage) {
		return new BalsaCircuit(storage);
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new BalsaVisualModelDescriptor();
	}
}
