package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.interop.AbstractSVGExporter;

public class BuiltinExporters implements Module {
	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();
		
		p.registerClass(Exporter.class, AbstractSVGExporter.class);
	}

	@Override
	public String getDescription() {
		return "Built-in exporters for Workcraft models";
	}

}
