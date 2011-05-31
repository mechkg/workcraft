package org.workcraft;

import org.workcraft.interop.GlobalService;
import org.workcraft.interop.ModelServices;
import org.workcraft.util.Maybe;

public interface Loader {
	GlobalService<Loader> SERVICE_HANDLE = GlobalService.createNewService(Loader.class, "Loader");
	
	Maybe<ModelServices> load(byte [] input);
}
