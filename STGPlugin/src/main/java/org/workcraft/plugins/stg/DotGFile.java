package org.workcraft.plugins.stg;

import java.io.File;

import org.workcraft.interop.ModelService;

public interface DotGFile {
	public File getFile();
	public static ModelService<DotGFile> SERVICE_HANDLE = ModelService.createNewService(DotGFile.class, ".g file"); 
}
