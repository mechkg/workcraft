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
import org.workcraft.util.Initialiser;

public class STGModule implements Module {

	@Override
	public void init(Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(ModelDescriptor.class, STGModelDescriptor.class);
		
		p.registerClass(XMLSerialiser.class, ImplicitPlaceArcSerialiser.class);
		p.registerClass(XMLDeserialiser.class, ImplicitPlaceArcDeserialiser.class);
		
		p.registerClass(Exporter.class, new Initialiser<Exporter>(){

			@Override
			public Exporter create() {
				return new DotGExporter();
			}} );
		p.registerClass(Importer.class, DotGImporter.class);
		
		p.registerClass(SettingsPage.class, STGSettings.class);
	}

	@Override
	public String getDescription() {
		return "Signal Transition Graphs";
	}
}
