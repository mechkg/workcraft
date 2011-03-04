package org.workcraft.plugins.petrify.tasks;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.ExportJob;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.interop.ServiceProvider;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.PetrifyTask;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyDummyContractionTask implements Task<PetrifyDummyContractionResult>{
	private final ExportJob dotGExportJob;
	private final TaskManager taskManager;

	public PetrifyDummyContractionTask(Framework framework, WorkspaceEntry workspaceEntry) throws ServiceNotAvailableException {
		this.taskManager = framework.getTaskManager();
		this.dotGExportJob = Export.chooseBestExporter(framework.getPluginManager(), workspaceEntry.getModelEntry(), Format.STG);
	}

	@Override
	public Result<? extends PetrifyDummyContractionResult> run(ProgressMonitor<? super PetrifyDummyContractionResult> monitor) {
		try
		{
			File tmp = File.createTempFile("stg_", ".g");

			ExportTask exportTask = new ExportTask(dotGExportJob, tmp);

			final Result<? extends Object> exportResult = taskManager.execute(exportTask, "Dummy contraction: writing .g");

			if (exportResult.getOutcome() != Outcome.FINISHED)
				if (exportResult.getOutcome() == Outcome.CANCELLED)
					return Result.cancelled();
				else
					return Result.exception(exportResult.getCause());

			PetrifyTask petrifyTask = new PetrifyTask(new String[] { "-hide", ".dummy" }, tmp.getAbsolutePath());

			final Result<? extends ExternalProcessResult> petrifyResult = taskManager.execute(petrifyTask, "Dummy contraction: executing Petrify");

			if (petrifyResult.getOutcome() == Outcome.FINISHED)
			{
				try {
					final ServiceProvider stg = new DotGImporter().importFrom(new ByteArrayInputStream(petrifyResult.getReturnValue().getOutput()));
					return Result.finished(new PetrifyDummyContractionResult(null, stg));
				} catch (DeserialisationException e) {
					return Result.exception(e);
				}

			} else
			{
				if(petrifyResult.getOutcome() == Outcome.FAILED)
					return Result.failed(new PetrifyDummyContractionResult(petrifyResult, null));
				else
					return Result.cancelled();
			}
		} catch (Throwable e)
		{
			return Result.exception(e);
		}
	}
}
