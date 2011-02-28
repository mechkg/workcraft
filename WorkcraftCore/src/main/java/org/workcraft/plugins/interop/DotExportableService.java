package org.workcraft.plugins.interop;

import org.workcraft.dom.Model;
import org.workcraft.interop.ServiceHandle;

public interface DotExportableService {
	public ServiceHandle<DotExportableService> SERVICE_HANDLE = ServiceHandle.createNewService(DotExportableService.class, "Something that can be exported to a DOT graph format");

	// TODO: put something more specific here, or remove the DotExportableService interface altogether (why would one want to export to DOT without doing a layout?)
	Model getModel();
}
