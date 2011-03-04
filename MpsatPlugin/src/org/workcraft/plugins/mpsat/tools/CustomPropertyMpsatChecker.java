package org.workcraft.plugins.mpsat.tools;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.ToolJob;
import org.workcraft.interop.ExportJob;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatSettingsSerialiser;
import org.workcraft.plugins.mpsat.gui.MpsatConfigurationDialog;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

public class CustomPropertyMpsatChecker implements Tool {

	public CustomPropertyMpsatChecker(Framework framework) {
		this.framework = framework;
	}
	
	private final Framework framework;

	@Override
	public String getSection() {
		return "Verification";
	}
	
	@Override
	public ToolJob applyTo(final WorkspaceEntry we) throws ServiceNotAvailableException {
		final ExportJob dotGExporter = Export.chooseBestExporter(framework.getPluginManager(), we.getModelEntry(), Format.STG);
		return new ToolJob(){
			@Override
			public void run() {
				PresetManager<MpsatSettings> pmgr = new PresetManager<MpsatSettings>(new File("config/mpsat_presets.xml"), new MpsatSettingsSerialiser());
				MpsatConfigurationDialog dialog = new MpsatConfigurationDialog(framework.getMainWindow(), pmgr);
				GUI.centerAndSizeToParent(dialog, framework.getMainWindow());
				dialog.setVisible(true);
				if (dialog.getModalResult() == 1)
				{
					final MpsatChainTask mpsatTask = new MpsatChainTask(dotGExporter, dialog.getSettings(), framework);
					framework.getTaskManager().queue(mpsatTask, "MPSat tool chain", 
							new MpsatChainResultHandler(framework, we));
				}
			}
		};
	}

	@Override
	public String getDisplayName() {
		return "Check custom property (punf, MPSat)";
	}
}