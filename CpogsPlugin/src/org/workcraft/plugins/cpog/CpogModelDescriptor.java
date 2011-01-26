package org.workcraft.plugins.cpog;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;

public class CpogModelDescriptor implements ModelDescriptor {
	@Override
	public org.workcraft.dom.math.MathModel createMathModel(StorageManager storage) {
		return new CPOG(storage);
	}

	@Override
	public String getDisplayName() {
		return "Conditional Partial Order Graph";
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new VisualCpogModelDescriptor();
	}
};
