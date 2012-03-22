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

package org.workcraft.interop;
import org.workcraft.serialisation.Format;



public interface Exporter {
	
	public static final GlobalService<Exporter> SERVICE_HANDLE = GlobalService.createNewService(Exporter.class, "Exporter");
	
	public static final int GENERAL_COMPATIBILITY = 1;
	public static final int BEST_COMPATIBILITY = 10;
	
	public String getDescription();
	public String getExtenstion();
	public Format getTargetFormat();
	/**
	 * Returns the export job which exports the model specified by its ServiceProvider.
	 * @param modelServices
	 * The services provided by the model.
	 * @return
	 * The job to export the model
	 * @throws ServiceNotAvailableException
	 */
	public ExportJob getExportJob(ModelServices modelServices) throws ServiceNotAvailableException;
	
	public class Util {
		public static  ExportJob tryGetExportJob(Exporter exporter, ModelServices modelServices) {
			try { return exporter.getExportJob(modelServices); }
			catch (ServiceNotAvailableException ex) { return null; }
		}
	}
}