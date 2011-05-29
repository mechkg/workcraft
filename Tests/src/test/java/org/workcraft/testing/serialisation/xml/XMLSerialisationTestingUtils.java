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

package org.workcraft.testing.serialisation.xml;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.ModuleInfo;
import org.workcraft.PluginManager;
import org.workcraft.PluginProvider;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.serialisation.xml.AffineTransformDeserialiser;
import org.workcraft.plugins.serialisation.xml.AffineTransformSerialiser;
import org.workcraft.plugins.serialisation.xml.BooleanDeserialiser;
import org.workcraft.plugins.serialisation.xml.BooleanSerialiser;
import org.workcraft.plugins.serialisation.xml.ConnectionDeserialiser;
import org.workcraft.plugins.serialisation.xml.ConnectionSerialiser;
import org.workcraft.plugins.serialisation.xml.DoubleDeserialiser;
import org.workcraft.plugins.serialisation.xml.DoubleSerialiser;
import org.workcraft.plugins.serialisation.xml.EnumDeserialiser;
import org.workcraft.plugins.serialisation.xml.EnumSerialiser;
import org.workcraft.plugins.serialisation.xml.IntDeserialiser;
import org.workcraft.plugins.serialisation.xml.IntSerialiser;
import org.workcraft.plugins.serialisation.xml.StringDeserialiser;
import org.workcraft.plugins.serialisation.xml.StringSerialiser;
import org.workcraft.plugins.serialisation.xml.VisualConnectionDeserialiser;
import org.workcraft.plugins.serialisation.xml.VisualConnectionSerialiser;
import org.workcraft.plugins.stg.DefaultStorageManager;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcDeserialiser;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcSerialiser;
import org.workcraft.util.Initialiser;

public class XMLSerialisationTestingUtils {
	static class MockPluginManager implements PluginProvider {
		@SuppressWarnings("unchecked")
		@Override
		public <T> Collection<PluginInfo<? extends T>> getPlugins(Class<T> interfaceType) {
			Initialiser<Object> [] legacy = getLegacyPlugins(interfaceType);
			ArrayList<PluginInfo<? extends T>> result = new ArrayList<PluginInfo<? extends T>>();
			for(Initialiser<Object> l : legacy)
				result.add(new PluginManager.PluginInstanceHolder<T>((Initialiser<? extends T>) l));
			return result;
		}

		public ModuleInfo[] getLegacyPlugins(Class<?> interfaceType) {
	
			if (interfaceType.equals(org.workcraft.serialisation.xml.XMLSerialiser.class))
			{
				return new ModuleInfo[] {
						new ModuleInfo (IntSerialiser.class),
						new ModuleInfo (BooleanSerialiser.class),
						new ModuleInfo (StringSerialiser.class),
						new ModuleInfo (DoubleSerialiser.class),
						new ModuleInfo (ConnectionSerialiser.class),
						new ModuleInfo (IntSerialiser.class),
						new ModuleInfo (EnumSerialiser.class),
						new ModuleInfo (AffineTransformSerialiser.class),
						new ModuleInfo (VisualConnectionSerialiser.class),
						new ModuleInfo (ImplicitPlaceArcSerialiser.class)
				};
			} else if (interfaceType.equals(org.workcraft.serialisation.xml.XMLDeserialiser.class))
			{
				return new ModuleInfo[] {
						new ModuleInfo (IntDeserialiser.class),
						new ModuleInfo (BooleanDeserialiser.class),
						new ModuleInfo (StringDeserialiser.class),
						new ModuleInfo (DoubleDeserialiser.class),
						new ModuleInfo (ConnectionDeserialiser.class),
						new ModuleInfo (IntDeserialiser.class),
						new ModuleInfo (EnumDeserialiser.class),
						new ModuleInfo (AffineTransformDeserialiser.class),
						new ModuleInfo (VisualConnectionDeserialiser.class),
						new ModuleInfo (ImplicitPlaceArcDeserialiser.class),
				};
			} else
				throw new RuntimeException ("Mock plugin manager doesn't know interface " + interfaceType.getCanonicalName());
		}

	}

	public static PluginProvider createMockPluginManager() {
		return new MockPluginManager();
	}

	public static STG createTestSTG1() {
		try {		
			STG stg = new STG(new HistoryPreservingStorageManager());

			Place p1 = stg.createPlace();
			Place p2 = stg.createPlace();

			SignalTransition t1 = stg.createSignalTransition();
			SignalTransition t2 = stg.createSignalTransition();


			stg.connect(t1, p1);

			stg.connect(p1, t2);
			stg.connect(t2, p2);
			stg.connect(p2, t1);

			return stg;
		}
		catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}		
	}
	
	public static STG createTestSTG2() {
		try {		
			STG stg = new STG(new HistoryPreservingStorageManager());

			Place p1 = stg.createPlace();
			Place p2 = stg.createPlace();

			SignalTransition t1 = stg.createSignalTransition();
			SignalTransition t2 = stg.createSignalTransition();


			stg.connect(t1, p1);

			stg.connect(p1, t2);
			stg.connect(t2, p2);
			stg.connect(p2, t1);

			MathGroup g1 = new MathGroup(new DefaultStorageManager());

			Place p3 = new Place(new DefaultStorageManager());
			SignalTransition t3 = new SignalTransition(new DefaultStorageManager());
			
			g1.add(p3); g1.add(t3);
			
			stg.connect(p3, t3);

			return stg;
		}
		catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}		
	}
	
	public static VisualSTG createTestSTG3() {
		try {		
			HistoryPreservingStorageManager storage = new HistoryPreservingStorageManager();
			STG stg = new STG(storage);

			SignalTransition t1 = stg.createSignalTransition();
			SignalTransition t2 = stg.createSignalTransition();
			SignalTransition t3 = stg.createSignalTransition();
			SignalTransition t4 = stg.createSignalTransition();
			
			VisualSTG visualSTG = new VisualSTG(stg, storage);
			
			VisualSignalTransition vt1 = new VisualSignalTransition(t1, new DefaultStorageManager());
			VisualSignalTransition vt2 = new VisualSignalTransition(t2, new DefaultStorageManager());
			VisualSignalTransition vt3 = new VisualSignalTransition(t3, new DefaultStorageManager());
			VisualSignalTransition vt4 = new VisualSignalTransition(t4, new DefaultStorageManager());
			
			visualSTG.add(vt1);visualSTG.add(vt2);visualSTG.add(vt3);visualSTG.add(vt4);
			
			visualSTG.connectionManager().connect(vt1, vt2);
			visualSTG.connectionManager().connect(vt2, vt3);
			visualSTG.connectionManager().connect(vt3, vt4);
			visualSTG.connectionManager().connect(vt4, vt1);
			
			
			
			return visualSTG;
		}
		catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}		
	}
}