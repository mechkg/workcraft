package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.plugins.workspace.handlers.SystemOpen;
import org.workcraft.plugins.workspace.handlers.WorkcraftOpen;
import org.workcraft.workspace.FileHandler;

public class BuiltinFileHandlers implements Module {
	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(FileHandler.SERVICE_HANDLE, new WorkcraftOpen(framework));
		p.registerClass(FileHandler.SERVICE_HANDLE, new SystemOpen());
	}

	@Override
	public String getDescription() {
		return "Built-in file operations for Workspace";
	}
}
