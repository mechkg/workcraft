package org.workcraft.plugins.balsa;

import org.workcraft.BalsaModelDescriptor;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.balsa.io.BreezeImporter;
import org.workcraft.plugins.balsa.io.DotExporter;

import tools.ExtractSTG;

public class BalsaPlugin implements Module {
	@Override
	public void init(final Framework framework) {
		final PluginManager pluginManager = framework.getPluginManager();

		pluginManager.registerClass(ModelDescriptor.GLOBAL_SERVICE_HANDLE, new BalsaModelDescriptor());
		pluginManager.registerClass(Exporter.SERVICE_HANDLE, new DotExporter());
		pluginManager.registerClass(Importer.SERVICE_HANDLE, new BreezeImporter());
		pluginManager.registerClass(Tool.SERVICE_HANDLE, new ExtractSTG(framework));
	}

	@Override
	public String getDescription() {
		return "Breeze handshake circuits";
	};
}