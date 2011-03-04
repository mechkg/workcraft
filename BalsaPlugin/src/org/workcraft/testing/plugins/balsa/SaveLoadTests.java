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

package org.workcraft.testing.plugins.balsa;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.Assert;
import org.workcraft.BalsaModelDescriptor;
import org.workcraft.Framework;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.LoadFromXMLException;
import org.workcraft.exceptions.ModelSaveFailedException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.BreezeHandshake;
import org.workcraft.plugins.balsa.VisualBalsaCircuit;
import org.workcraft.plugins.balsa.VisualBreezeComponent;
import org.workcraft.plugins.balsa.VisualHandshake;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.stg.DefaultStorageManager;

public class SaveLoadTests {
	//TODO: Re-write tests
	//@Test
	public void TestMathModelLoad() throws Exception
	{
		testMathModelLoadWhileWhile(new FileInputStream("./org/workcraft/testing/plugins/balsa/tests/LoopWhile.work"));
	}
	
	void testMathModelLoadWhileWhile(InputStream input) throws LoadFromXMLException, DeserialisationException, IOException, FormatException, PluginInstantiationException, ServiceNotAvailableException
	{
		Framework framework = new Framework();
		framework.getPluginManager().loadManifest();
		
		BalsaCircuit circuit = framework.load(input).getImplementation(BalsaCircuit.SERVICE_HANDLE);

		Assert.assertNotNull(circuit);
		
		Node[] components = eval(circuit.getRoot().children()).toArray(new Node[0]);
		
		//TODO: fix the test, considering hierarchical math model structure
		Assert.assertEquals(7, components.length);
		
		ArrayList<BreezeComponent> brz = new ArrayList<BreezeComponent>();
		
		for(int i=0;i<components.length;i++)
			if(components[i] instanceof BreezeComponent)
				brz.add((BreezeComponent)components[i]);
		
		Assert.assertEquals(2, brz.size());
		
		BreezeComponent brz0 = brz.get(0);
		BreezeComponent brz1 = brz.get(1);
		
		BreezeComponent loop;
		BreezeComponent wh;
		
		if(brz0.getUnderlyingComponent().declaration().getName().equals("Loop"))
		{
			loop = brz0;
			wh = brz1;
		}
		else
		{
			loop = brz1;
			wh = brz0;
		}
		
		//Assert.assertEquals(Loop.class, loop.getUnderlyingComponent().getClass());
		//Assert.assertEquals(While.class, wh.getUnderlyingComponent().getClass());
		//
		Assert.assertTrue(
			circuit.getConnectedHandshake(loop.getHandshakeComponentByName("activateOut")) == wh.getHandshakeComponentByName("activate")
		);
	}
	
	//@Test
	public void TestVisualModelLoad() throws Exception
	{
		//Model model = Framework.load("./org/workcraft/testing/plugins/balsa/tests/LoopWhile_Visual.work");
		//testVisualModelLoopWhile(model);
	}

	private void testVisualModelLoopWhile(Model model) {
		VisualBalsaCircuit visual = (VisualBalsaCircuit)model;
		BalsaCircuit circuit = (BalsaCircuit)visual.getMathModel();
		
		Assert.assertNotNull(circuit);
		Assert.assertNotNull(visual);
		
		Assert.assertEquals(3, eval(visual.getRoot().children()).size());
		
		VisualConnection con = null;
		VisualBreezeComponent wh = null;
		VisualBreezeComponent loop = null;
		
		for(Node node : eval(visual.getRoot().children()))
			if(node instanceof VisualConnection)
				con = (VisualConnection)node;
			else if(node instanceof VisualBreezeComponent)
			{
				VisualBreezeComponent brz = (VisualBreezeComponent)node;
				if(brz.getRefComponent().getUnderlyingComponent().declaration().getName().equals("Loop"))
					loop = brz;
				if(brz.getRefComponent().getUnderlyingComponent().declaration().getName().equals("While"))
					wh = brz;
			}
		
		Assert.assertNotNull(con);
		Assert.assertNotNull(wh);
		Assert.assertNotNull(loop);
		
		Assert.assertEquals(3, eval(wh.children()).size());
		Assert.assertEquals(2, eval(loop.children()).size());
		
		VisualHandshake whActivate = getVisualHandshakeByName(wh, "activate");
		VisualHandshake loopActivateOut = getVisualHandshakeByName(loop, "activateOut");
		
		Assert.assertTrue(
				con.getFirst() == whActivate && con.getSecond() == loopActivateOut || 
				con.getSecond() == whActivate && con.getFirst() == loopActivateOut 
				);
		
		Assert.assertEquals(0.0, eval(loop.x()), 0.5);
		Assert.assertEquals(10.0, eval(wh.x()), 0.5);
	}

	private VisualHandshake getVisualHandshakeByName(VisualBreezeComponent wh, String name) {
		for(Node component : eval(wh.children()))
		{
			VisualHandshake handshake = (VisualHandshake) component;
			if(((BreezeHandshake)handshake.getReferencedComponent()).getHandshakeName().equals(name))
				return handshake;
		}
		return null;
	}
	
	//@Test
	public void TestMathModelSaveLoad() throws InvalidConnectionException, ModelSaveFailedException, LoadFromXMLException, IOException, ModelValidationException, SerialisationException, ServiceNotAvailableException
	{
		BalsaCircuit circuit = createWhileWhileMathCircuit();
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		new Framework().save(BalsaModelDescriptor.getServices(circuit, new DefaultStorageManager()), stream);
		
		//testMathModelLoadWhileWhile(new ByteArrayInputStream(stream.toByteArray()));
	}

	//@Test
	public void TestVisualModelSaveLoad() throws InvalidConnectionException, ModelSaveFailedException, LoadFromXMLException, VisualModelInstantiationException, IOException, ModelValidationException, SerialisationException, ServiceNotAvailableException
	{
		VisualBalsaCircuit circuit = createLoopWhileVisualCircuit();
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		//FileOutputStream temp = new FileOutputStream("temp.work");
		//new Framework().save(circuit, temp);
		//temp.close();
		new Framework().save(BalsaModelDescriptor.getServices(circuit, new DefaultStorageManager()), stream);
		testVisualModelLoopWhile(circuit);
		/*testVisualModelLoopWhile(
				Framework.load(
				new ByteArrayInputStream(stream.toByteArray())));*/
	}

	private VisualBalsaCircuit createLoopWhileVisualCircuit() throws VisualModelInstantiationException, InvalidConnectionException {
		DefaultStorageManager storage = new DefaultStorageManager();
		BalsaCircuit math = new BalsaCircuit(storage); 
		
		VisualBalsaCircuit visual = new VisualBalsaCircuit(math, storage);
		
		BreezeComponent wh = new BreezeComponent(storage);
		wh.setUnderlyingComponent(createWhile());
		BreezeComponent loop = new BreezeComponent(storage);
		loop.setUnderlyingComponent(createLoop());
		math.add(wh);
		math.add(loop);
		MathConnection con = (MathConnection)math.connect(loop.getHandshakeComponentByName("activateOut"), wh.getHandshakeComponentByName("activate"));
		
		VisualBreezeComponent whVis = new VisualBreezeComponent(wh, storage);
		whVis.x().setValue(10.0);
		visual.add(whVis);
		VisualBreezeComponent loopVis = new VisualBreezeComponent(loop, storage);
		visual.add(loopVis);
		
		VisualConnection conVis = new VisualConnection(con, getVisualHandshakeByName(loopVis, "activateOut"),
				getVisualHandshakeByName(whVis, "activate"), storage);
		visual.add(conVis);
		
		return visual;
	}

	private DynamicComponent createWhile() {
		return create("While");
	}

	private DynamicComponent createLoop() {
		return create("Loop");
	}

	private DynamicComponent create(String name) {
		throw new NotImplementedException();
		//return  new BreezeLibrary(BalsaSystem.DEFAULT()).getPrimitive(name).instantiate();
	}

	private BalsaCircuit createWhileWhileMathCircuit()
			throws InvalidConnectionException {
		BalsaCircuit circuit = new BalsaCircuit(new DefaultStorageManager());
		
		BreezeComponent wh = new BreezeComponent(new DefaultStorageManager());
	
		wh.setUnderlyingComponent(createWhile());
		BreezeComponent loop = new BreezeComponent(new DefaultStorageManager());
		loop.setUnderlyingComponent(createLoop());
		circuit.add(wh);
		circuit.add(loop);
		circuit.connect(loop.getHandshakeComponentByName("activateOut"), wh.getHandshakeComponentByName("activate"));
		return circuit;
	}

	
//	@Test
	public void TestMathModelSaveLoadSaveLoad() throws InvalidConnectionException, ModelSaveFailedException, LoadFromXMLException, IOException, ModelValidationException, SerialisationException, FormatException, DeserialisationException, PluginInstantiationException, ServiceNotAvailableException 
	{
		Framework f = new Framework();
		f.getPluginManager().loadManifest();
		
		BalsaCircuit circuit = createWhileWhileMathCircuit();
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		StorageManager storage = new DefaultStorageManager();
		f.save(BalsaModelDescriptor.getServices(circuit, storage), stream);
		
		BalsaCircuit loaded = f.load(new ByteArrayInputStream(stream.toByteArray())).getImplementation(BalsaCircuit.SERVICE_HANDLE);
		
		stream = new ByteArrayOutputStream();
		
		f.save(BalsaModelDescriptor.getServices(loaded, storage), stream);
		
		testMathModelLoadWhileWhile(new ByteArrayInputStream(stream.toByteArray()));
	}
}
