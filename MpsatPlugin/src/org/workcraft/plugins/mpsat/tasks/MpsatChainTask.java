package org.workcraft.plugins.mpsat.tasks;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.interop.ExportJob;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatChainTask implements Task<MpsatChainResult> {
	private final MpsatSettings settings;
	private final Framework framework;
	private ExportJob dotGExportJob;

	public static MpsatChainTask create(WorkspaceEntry we, MpsatSettings settings, Framework framework) throws ServiceNotAvailableException {
		ExportJob dotGExportJob = Export.chooseBestExporter(framework.getPluginManager(), we.getModelEntry(), Format.STG);
		return new MpsatChainTask(dotGExportJob, settings, framework);
	}
	
	public MpsatChainTask(ExportJob dotGExportJob, MpsatSettings settings, Framework framework) {
		this.dotGExportJob = dotGExportJob;
		this.settings = settings;
		this.framework = framework;
	}
	
	@Override
	public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
		try {
			File netFile = File.createTempFile("net", ".g");
			
			ExportTask exportTask;
			
			exportTask = new ExportTask(dotGExportJob, netFile);
			
			SubtaskMonitor<Object> mon = new SubtaskMonitor<Object>(monitor);
			
			Result<? extends Object> exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g", mon);

			if (exportResult.getOutcome() != Outcome.FINISHED) {
				netFile.delete();
				if (exportResult.getOutcome() == Outcome.CANCELLED)
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				return new Result<MpsatChainResult>(Outcome.FAILED, new MpsatChainResult(exportResult, null, null, settings));
			}
			
			monitor.progressUpdate(0.33);
			
			File mciFile = File.createTempFile("unfolding", ".mci");

			PunfTask punfTask = new PunfTask(netFile.getCanonicalPath(), mciFile.getCanonicalPath());
			Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(punfTask, "Unfolding .g", mon);
			netFile.delete();
			
			if (punfResult.getOutcome() != Outcome.FINISHED) {
				mciFile.delete();
				if (punfResult.getOutcome() == Outcome.CANCELLED)
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				return new Result<MpsatChainResult>(Outcome.FAILED, new MpsatChainResult(exportResult, punfResult, null, settings));
			}

			monitor.progressUpdate(0.66);

			MpsatTask mpsatTask = new MpsatTask(settings.getMpsatArguments(), mciFile.getCanonicalPath());
			Result<? extends ExternalProcessResult> mpsatResult = framework.getTaskManager().execute(mpsatTask, "Running mpsat model-checking", mon);
			mciFile.delete();
			
			if (mpsatResult.getOutcome() != Outcome.FINISHED) {
				if (mpsatResult.getOutcome() == Outcome.CANCELLED)
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				return new Result<MpsatChainResult>(Outcome.FAILED, new MpsatChainResult(exportResult, punfResult, mpsatResult, settings ));
			}
		
			monitor.progressUpdate(1.0);
			
			return new Result<MpsatChainResult>(Outcome.FINISHED, new MpsatChainResult(exportResult, punfResult, mpsatResult, settings));
		} catch (Throwable e) {
			return new Result<MpsatChainResult>(e);
		}
	}

	public MpsatSettings getSettings() {
		return settings;
	}

	public Framework getFramework() {
		return framework;
	}
}
