package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.plugins.serialisation.xml.AffineTransformDeserialiser;
import org.workcraft.plugins.serialisation.xml.AffineTransformSerialiser;
import org.workcraft.plugins.serialisation.xml.BooleanDeserialiser;
import org.workcraft.plugins.serialisation.xml.BooleanSerialiser;
import org.workcraft.plugins.serialisation.xml.DoubleDeserialiser;
import org.workcraft.plugins.serialisation.xml.DoubleSerialiser;
import org.workcraft.plugins.serialisation.xml.EnumDeserialiser;
import org.workcraft.plugins.serialisation.xml.EnumSerialiser;
import org.workcraft.plugins.serialisation.xml.IntDeserialiser;
import org.workcraft.plugins.serialisation.xml.IntSerialiser;
import org.workcraft.plugins.serialisation.xml.StringDeserialiser;
import org.workcraft.plugins.serialisation.xml.StringSerialiser;

public class BuiltinSerialisers implements Module {
	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();

		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.SERVICE_HANDLE, new AffineTransformSerialiser());
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.SERVICE_HANDLE, new BooleanSerialiser());
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.SERVICE_HANDLE, new DoubleSerialiser());
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.SERVICE_HANDLE, new EnumSerialiser());
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.SERVICE_HANDLE, new IntSerialiser());
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.SERVICE_HANDLE, new StringSerialiser());
/*		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.SERVICE_HANDLE, new BezierSerialiser());
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.SERVICE_HANDLE, new ConnectionSerialiser());
		p.registerClass(org.workcraft.serialisation.xml.XMLSerialiser.SERVICE_HANDLE, new VisualConnectionSerialiser());*/
		
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.SERVICE_HANDLE, new AffineTransformDeserialiser());
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.SERVICE_HANDLE, new BooleanDeserialiser());
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.SERVICE_HANDLE, new DoubleDeserialiser());
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.SERVICE_HANDLE, new EnumDeserialiser());
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.SERVICE_HANDLE, new IntDeserialiser());
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.SERVICE_HANDLE, new StringDeserialiser());
/*		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.SERVICE_HANDLE, new BezierDeserialiser());
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.SERVICE_HANDLE, new ConnectionDeserialiser());
		p.registerClass(org.workcraft.serialisation.xml.XMLDeserialiser.SERVICE_HANDLE, new VisualConnectionDeserialiser());*/
	}

	@Override
	public String getDescription() {
		return "Built-in XML serialisers for basic data types";
	}
}
