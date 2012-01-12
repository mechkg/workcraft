package org.workcraft.plugins.mpsat;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.Tool;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.plugins.mpsat.tools.CscResolutionTool;
import org.workcraft.plugins.mpsat.tools.CustomPropertyMpsatChecker;
import org.workcraft.plugins.mpsat.tools.MpsatDeadlockChecker;
import org.workcraft.plugins.mpsat.tools.MpsatSynthesis;
import org.workcraft.plugins.pcomp.PcompUtilitySettings;
import org.workcraft.plugins.pcomp.tools.PcompTool;

public class MPSatModule implements Module {

	@Override
	public void init(Framework framework) {
		framework.getPluginManager().registerClass(Tool.SERVICE_HANDLE, new CscResolutionTool(framework));
		framework.getPluginManager().registerClass(Tool.SERVICE_HANDLE, new MpsatSynthesis(framework));
		framework.getPluginManager().registerClass(Tool.SERVICE_HANDLE, new MpsatDeadlockChecker(framework));
		framework.getPluginManager().registerClass(Tool.SERVICE_HANDLE, new CustomPropertyMpsatChecker(framework));
		
		framework.getPluginManager().registerClass(SettingsPage.SERVICE_HANDLE, new MpsatUtilitySettings());
		framework.getPluginManager().registerClass(SettingsPage.SERVICE_HANDLE, new PunfUtilitySettings());
		
		framework.getPluginManager().registerClass(Tool.SERVICE_HANDLE, new PcompTool(framework));
		framework.getPluginManager().registerClass(SettingsPage.SERVICE_HANDLE, new PcompUtilitySettings());
	}

	@Override
	public String getDescription() {
		return "Punf, MPSat and PComp tool support";
	}
}
