package org.workcraft.plugins.graph;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.interop.ServiceProviderImpl;

public class GraphModelDescriptor implements ModelDescriptor
{
	@Override
	public String getDisplayName() {
		return "Directed Graph";
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new VisualGraphModelDescriptor();
	}

	@Override
	public MathModel createMathModel(StorageManager storage) {
		return new Graph(storage);
	}

	@Override
	public ServiceProvider createServiceProvider(Model model) {
		return ServiceProviderImpl.createLegacyServiceProvider(model);
	}
}