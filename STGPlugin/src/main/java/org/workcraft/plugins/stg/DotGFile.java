package org.workcraft.plugins.stg;

import java.io.File;

import org.workcraft.interop.ServiceHandle;

public interface DotGFile {
	public File getFile();
	public static ServiceHandle<DotGFile> SERVICE_HANDLE = ServiceHandle.createNewService(DotGFile.class, ".g file"); 
}
