package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
//import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.plugins.layout.DotLayoutSettings;
import org.workcraft.plugins.layout.NullLayout;
import org.workcraft.plugins.layout.RandomLayout;
import org.workcraft.plugins.layout.RandomLayoutSettings;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class BuiltinTools implements Module {
	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();
		//p.registerClass(Tool.SERVICE_HANDLE, DotLayout.SERVICE_HANDLE, framework);
		p.registerClass(Tool.SERVICE_HANDLE, new NullLayout());
		p.registerClass(Tool.SERVICE_HANDLE, new RandomLayout());
		
		//p.registerClass(SettingsPage.SERVICE_HANDLE, new DotLayoutSettings());
		//p.registerClass(SettingsPage.SERVICE_HANDLE, new RandomLayoutSettings());
		//p.registerClass(SettingsPage.SERVICE_HANDLE, new CommonVisualSettings());
	}

	@Override
	public String getDescription() {
		return "Built-in tools";
	}
}
