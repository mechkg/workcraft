package org.workcraft.plugins.interop;

import org.workcraft.dom.Model;
import org.workcraft.interop.ModelService;

public interface DotExportableService {
	public ModelService<DotExportableService> SERVICE_HANDLE = ModelService.createNewService(DotExportableService.class, "Something that can be exported to a DOT graph format");

	// TODO: put something more specific here, or remove the DotExportableService interface altogether (why would one want to export to DOT without doing a layout?)
	Model getModel();
}
