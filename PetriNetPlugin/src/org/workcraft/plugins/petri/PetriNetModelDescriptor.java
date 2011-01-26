package org.workcraft.plugins.petri;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class PetriNetModelDescriptor implements ModelDescriptor
{
	@Override
	public String getDisplayName() {
		return "Petri Net";
	}

	@Override
	public MathModel createMathModel(StorageManager storage) {
		return new PetriNet(storage);
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new PetriNetVisualModelDescriptor();
	}
}
