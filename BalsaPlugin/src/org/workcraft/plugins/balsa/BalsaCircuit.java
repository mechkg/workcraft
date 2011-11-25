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

package org.workcraft.plugins.balsa;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Connection;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.interop.ModelService;
import org.workcraft.interop.ServiceHandle;
import org.workcraft.parsers.breeze.Netlist;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakebuilder.DataHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.FullDataHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.FullDataPull;
import org.workcraft.plugins.balsa.handshakebuilder.FullDataPush;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakebuilder.PullHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.PushHandshake;
import org.workcraft.plugins.balsa.handshakes.MainHandshakeMaker;
import org.workcraft.util.Hierarchy;

public final class BalsaCircuit {

	public static ModelService<BalsaCircuit> SERVICE_HANDLE = ModelService.createNewService(BalsaCircuit.class, "A balsa circuit");
	
	public final StorageManager storage;

	public BalsaCircuit(StorageManager storage) {
		this.storage = storage;
		
		//TODO: do this somehow
				//if(e instanceof NodesAddedEvent)
					/*for(Node node : e.getAffectedNodes())
					{
						if(node instanceof BreezeComponent)
							createHandshakeComponents((BreezeComponent)node);
						if(node instanceof BreezeHandshake)
							handshakeAdded((BreezeHandshake)node);
					}*/
				// TODO: delete handshake components if needed
				// TODO: re-create handshakes if needed
	}

	private void handshakeAdded(BreezeHandshake component) {
		Map<Handshake, BreezeHandshake> handshakeComponents = component.getOwner().getHandshakeComponents();
		if(handshakeComponents == null)
			return;
		Handshake handshake = component.getHandshake();
		BreezeHandshake existing = handshakeComponents.get(handshake);
		if(existing == component)
			return;
		
		throw new NotImplementedException(); /*remove(existing);
		
		handshakeComponents.put(handshake, component);
		
		component.getOwner().setHandshakeComponents(handshakeComponents);*/
	}

	private void createHandshakeComponents(BreezeComponent component) {
		HashMap<Handshake, BreezeHandshake> handshakeComponents = new LinkedHashMap<Handshake, BreezeHandshake>(); 
		Map<String, Handshake> handshakes = MainHandshakeMaker.getHandshakes(component.getUnderlyingComponent());
		for(String handshakeName : handshakes.keySet())
		{
			Handshake handshake = handshakes.get(handshakeName);
			BreezeHandshake hcomp = new BreezeHandshake(component, handshakeName, storage);
			handshakeComponents.put(handshake, hcomp);
			if(true) throw new NotImplementedException(); //add(hcomp);
		}
		component.setHandshakeComponents(handshakeComponents);
		component.setHandshakes(handshakes);
	}

	public void validateConnection(BreezeHandshake first, BreezeHandshake second)
			throws InvalidConnectionException {
		
		if(true) throw new NotImplementedException(); //
		/*if(eval(nodeContext()).getPostset(first).size() > 0 || eval(nodeContext()).getPreset(first).size() > 0 ||
				eval(nodeContext()).getPostset(second).size() > 0 || eval(nodeContext()).getPreset(second).size() > 0)
			throw new InvalidConnectionException("Cannot connect already connected handshakes");
			*/
		Handshake h1 = first.getHandshake();
		Handshake h2 = second.getHandshake();
		
		if(h1.isActive() == h2.isActive())
			throw new InvalidConnectionException("Must connect passive and active handshakes. " + getHandshakesDescription(first, second));
		
		boolean isData1 = h1 instanceof DataHandshake;
		boolean isData2 = h2 instanceof DataHandshake;
		boolean isFull1 = h1 instanceof FullDataHandshake;
		boolean isFull2 = h2 instanceof FullDataHandshake;
		
		if((isData1 || isFull1) != (isData2 || isFull2))
			throw new InvalidConnectionException("Cannot connect data handshake with an activation handshake");
		
		if(isData1)
		{
			if(isFull1 != isFull2)
				throw new InvalidConnectionException("Cannot connect control-side data handshake with datapath-side data handshake");
			
			boolean push1 = isPush(h1);
			boolean push2 = isPush(h2);
			
			if(push1 != push2)
				throw new InvalidConnectionException("Cannot connect push handshake with pull handshake");
			
			if(isData1)
				if(((DataHandshake)h1).getWidth() != ((DataHandshake)h2).getWidth())
					throw new InvalidConnectionException("Cannot connect data handshakes with different bit widths");
			
			if(isFull1)
				if(((FullDataHandshake)h1).getValuesCount() != ((FullDataHandshake)h2).getValuesCount())
					throw new InvalidConnectionException("Cannot connect data handshakes with different value counts");
		}
	}
	
	/*public MathConnection connect (BreezeHandshake first, BreezeHandshake second) throws InvalidConnectionException {
		validateConnection(first, second);
		
		MathConnection con = new MathConnection(first, second, storage);
		
		add(Hierarchy.getNearestContainer(first, second), con);
		
		return con;
	}*/
	
	private String getHandshakesDescription(BreezeHandshake first, BreezeHandshake second) {
		return String.format("first: %s, %s; second: %s, %s", 
		first.getHandshakeName(), first.getOwner().getUnderlyingComponent().toString(),
		second.getHandshakeName(), second.getOwner().getUnderlyingComponent().toString());
	}

	private boolean isPush(Handshake h2)
	{
		if (h2 instanceof PushHandshake)
			return true;
		if (h2 instanceof PullHandshake)
			return false;
		if (h2 instanceof FullDataPush)
			return true;
		if (h2 instanceof FullDataPull)
			return false;
		throw new RuntimeException("Unknown data handshake type"); // return !true && !false; %)
	}

	public Connection getConnection(BreezeHandshake handshake) {
		Set<Connection> connections; if(true) throw new NotImplementedException(); // = eval(nodeContext()).getConnections(handshake);
		if(connections.size() > 1)
			throw new RuntimeException("Handshake can't have more than 1 connection!");
		if(connections.size() == 0)
			return null;
		return connections.iterator().next();
	}

	public final BreezeHandshake getConnectedHandshake(BreezeHandshake handshake) {
		Connection connection = getConnection(handshake);
		if (connection == null)
			return null;
		if (connection.getFirst() == handshake)
			return (BreezeHandshake) connection.getSecond();
		if (connection.getSecond() == handshake)
			return (BreezeHandshake) connection.getFirst();
		throw new RuntimeException("Invalid connection");
	}
	
	public final Collection<BreezeComponent> getComponents()
	{
		 throw new NotImplementedException(); //return Hierarchy.getChildrenOfType(this.getRoot(), BreezeComponent.class);
	}
	
	public BreezeComponent addNew(DynamicComponent component) 
	{
		BreezeComponent node = new BreezeComponent(storage);
		node.setUnderlyingComponent(component);
		if(true) throw new NotImplementedException(); //add(node);
		return node;
	}
	
/*	public Netlist<BreezeHandshake, BreezeComponent, BreezeConnection> asNetlist()
	{
		return new Netlist<BreezeHandshake, BreezeComponent, BreezeConnection>()
		{

			@Override public List<BreezeComponent> getBlocks() 
			{
				return new ArrayList<BreezeComponent>(getComponents());
			}

			@Override public List<BreezeConnection> getConnections() 
			{
				List<BreezeConnection> result = new ArrayList<BreezeConnection>();
				Collection<MathConnection> conns = BalsaCircuit.this.getConnections();
				for(MathConnection conn : conns)
					result.add(new BreezeConnection(conn));
				return result;
			}

			@Override public List<BreezeHandshake> getPorts() 
			{
				LinkedHashSet<BreezeHandshake> handshakes = new LinkedHashSet<BreezeHandshake>();
				for(BreezeComponent component : getComponents())
				{
					for(BreezeHandshake hs : component.getHandshakeComponents().values())
						handshakes.add(hs);
				}
				for(BreezeConnection conn : getConnections())
				{
					handshakes.remove(conn.getFirst());
					handshakes.remove(conn.getSecond());
				}
				return new ArrayList<BreezeHandshake>(handshakes);
			}
		};
	}*/
}
