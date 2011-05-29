package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;

public class BuiltinExporters implements Module {
	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();
	}

	@Override
	public String getDescription() {
		return "Built-in exporters for Workcraft models";
	}

}
