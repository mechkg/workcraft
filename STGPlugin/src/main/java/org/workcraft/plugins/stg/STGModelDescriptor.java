package org.workcraft.plugins.stg;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class STGModelDescriptor implements ModelDescriptor
{
	@Override
	public String getDisplayName() {
		return "Signal Transition Graph";
	}

	@Override
	public MathModel createMathModel(StorageManager storage) {
		return new STG(storage);
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new STGVisualModelDescriptor();
	}
}
