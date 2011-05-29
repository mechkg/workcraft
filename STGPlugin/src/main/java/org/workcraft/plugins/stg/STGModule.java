package org.workcraft.plugins.stg;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcDeserialiser;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcSerialiser;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class STGModule implements Module {

	@Override
	public void init(Framework framework) {
		final PluginManager p = framework.getPluginManager();
		
		p.registerClass(ModelDescriptor.GLOBAL_SERVICE_HANDLE, new STGModelDescriptor());
		
		p.registerClass(XMLSerialiser.SERVICE_HANDLE, new ImplicitPlaceArcSerialiser());
		p.registerClass(XMLDeserialiser.SERVICE_HANDLE, new ImplicitPlaceArcDeserialiser());
		
		p.registerClass(Exporter.SERVICE_HANDLE, new DotGExporter());
		p.registerClass(Importer.SERVICE_HANDLE, new DotGImporter());
		
		p.registerClass(SettingsPage.SERVICE_HANDLE, new STGSettings());
	}

	@Override
	public String getDescription() {
		return "Signal Transition Graphs";
	}
}
