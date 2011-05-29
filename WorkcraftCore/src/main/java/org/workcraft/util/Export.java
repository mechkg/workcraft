/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.workcraft.PluginProvider;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.ExportJob;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ModelServices;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;

public class Export {
	public static class ExportTask implements Task<Nothing> {
		ExportJob exporter;
		File file;
		
		public ExportTask(ExportJob exporter, File file) {
			this.exporter = exporter;
			this.file = file;
		}
		
		@Override
		public Result<? extends Nothing> run(ProgressMonitor<? super Nothing> monitor) {
			try {
				exportToFile(exporter, file);
			} catch (Throwable e) {
				return new Result<Nothing>(e);
			}
			
			return new Result<Nothing>(Outcome.FINISHED);
		}
	}
	
	static public ExportJob chooseBestExporter (PluginProvider provider, ModelServices modelServices, Format targetFormat) throws ServiceNotAvailableException {
		Iterable<Exporter> exporters = provider.getPlugins(Exporter.SERVICE_HANDLE);
		
		ExportJob best = null;
		int bestCompatibility = -1;
		
		for (Exporter exporter : exporters) {
			if (exporter.getTargetFormat().equals(targetFormat)) {
				ExportJob exportJob;
				try {
					exportJob = exporter.getExportJob(modelServices);
					int compatibility = exportJob.getCompatibility();
					
					if (best == null || compatibility > bestCompatibility) {
						bestCompatibility = compatibility;
						best = exportJob;
					}
				} catch (ServiceNotAvailableException e) {
				}
			}
		}

		if (best == null) // TODO: determine model type name?
			throw new ServiceNotAvailableException("No exporter available for the model " + modelServices + " to produce format " + Format.getDescription(targetFormat));
		return best;
	}
	
	static public void exportToFile (ModelServices modelServices, File file, Format targetFormat, PluginProvider provider) throws IOException, ModelValidationException, ServiceNotAvailableException, SerialisationException {
		ExportJob exporter = chooseBestExporter(provider, modelServices, targetFormat);
		exportToFile(exporter, file);
	}
	
	static public ExportTask createExportTask (ModelServices modelServices, File file, Format targetFormat, PluginProvider provider) throws ServiceNotAvailableException {
		ExportJob exporter = chooseBestExporter(provider, modelServices, targetFormat);
		return new ExportTask(exporter, file);
	}
	
	static public void exportToFile (ExportJob exporter, File file) throws IOException, ModelValidationException, SerialisationException {
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		
		boolean ok = false;
		
		try
		{
			exporter.export(fos);
			ok = true;
		}
		finally
		{
			fos.close();
			if(!ok)
				file.delete();
		}
	}
}
