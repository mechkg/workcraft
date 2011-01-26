package org.workcraft.plugins.graph;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

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
}