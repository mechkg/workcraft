package org.workcraft.plugins.cpog;

import org.workcraft.Framework;
import org.workcraft.Loader;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.dom.ModelDescriptor;

public class CpogModule implements Module {
	@Override
	public void init(Framework framework) {
		final PluginManager p = framework.getPluginManager();
		
		p.registerClass(ModelDescriptor.GLOBAL_SERVICE_HANDLE, new CpogModelDescriptor());
		p.registerClass(Loader.SERVICE_HANDLE, new CpogLoader());
		
		/*		p.registerClass(XMLSerialiser.class, VisualCPOGGroupSerialiser.class);
		p.registerClass(XMLSerialiser.class, VertexSerialiser.class);
		p.registerClass(XMLSerialiser.class, RhoClauseSerialiser.class);
		p.registerClass(XMLSerialiser.class, ArcSerialiser.class);
			
		p.registerClass(XMLDeserialiser.class, VisualCPOGGroupDeserialiser.class);
		p.registerClass(XMLDeserialiser.class, VertexDeserialiser.class);
		p.registerClass(XMLDeserialiser.class, RhoClauseDeserialiser.class);
		p.registerClass(XMLDeserialiser.class, ArcDeserialiser.class);*/
	
	}

	@Override
	public String getDescription() {
		return "Conditional Partial Order Graphs";
	}
}