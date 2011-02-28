package org.workcraft.plugins.workflow;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.interop.ServiceProvider;

public class WorkflowModelDescriptor implements ModelDescriptor {

	@Override
	public String getDisplayName() {
		return "Workflow";
	}

	@Override
	public MathModel createMathModel(StorageManager storage) {
		return new Workflow(storage);
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return null;
	}

	@Override
	public ServiceProvider createServiceProvider(Model model) {
		return null;
	}
}
