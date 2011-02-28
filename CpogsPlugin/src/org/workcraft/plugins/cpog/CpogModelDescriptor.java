package org.workcraft.plugins.cpog;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.interop.ServiceProviderImpl;

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

	@Override
	public ServiceProvider createServiceProvider(Model model) {
		return ServiceProviderImpl.createLegacyServiceProvider(model);
	}
};
