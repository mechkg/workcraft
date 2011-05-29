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

package org.workcraft.plugins.interop;

import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ModelServices;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.stg.serialisation.DotGSerialiser;
import org.workcraft.serialisation.Format;

public class DotGExporter implements Exporter {
	public final static class ExportJob implements org.workcraft.interop.ExportJob {
		private final STGModel model;

		public ExportJob(STGModel model) {
			this.model = model;
		}

		@Override
		public int getCompatibility() {
			return Exporter.BEST_COMPATIBILITY;
		}

		@Override
		public void export(final OutputStream out) throws IOException, ModelValidationException, SerialisationException {
			DotGSerialiser.serialise(model, out);
		}
	}
	@Override
	public String getDescription() {
		return DotGSerialiser.getExtension() + " (" + DotGSerialiser.getDescription() + ")";
	}

	@Override
	public String getExtenstion() {
		return DotGSerialiser.getExtension();
	}

	@Override
	public Format getTargetFormat() {
		return Format.STG;
	}

	@Override
	public ExportJob getExportJob(final ModelServices modelServices) throws ServiceNotAvailableException {

		final STGModel model = modelServices.getImplementation(STGModel.SERVICE_HANDLE);

		return new ExportJob(model);
	}
}
