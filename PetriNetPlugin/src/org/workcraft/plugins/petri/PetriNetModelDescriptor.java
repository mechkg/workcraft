package org.workcraft.plugins.petri;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;

public class PetriNetModelDescriptor implements ModelDescriptor
{
	@Override
	public String getDisplayName() {
		return "Petri Net";
	}

	@Override
	public MathModel createMathModel() {
		return new PetriNet(new HistoryPreservingStorageManager());
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new PetriNetVisualModelDescriptor();
	}
}
