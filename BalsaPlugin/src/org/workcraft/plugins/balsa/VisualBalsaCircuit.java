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

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.annotations.CustomTools;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.util.Hierarchy;

@CustomTools(VisualBalsaTools.class)
public final class VisualBalsaCircuit {
	public VisualBalsaCircuit(BalsaCircuit model, StorageManager storage) throws VisualModelInstantiationException {
		
		Map<MathNode, VisualHandshake> visuals = new HashMap<MathNode, VisualHandshake>();
		
		for(BreezeComponent component : model.getComponents())
		{
			VisualBreezeComponent visual = new VisualBreezeComponent(component, storage);
			if(true)throw new NotImplementedException();//add(visual);
			
			for(VisualHandshake hc : visual.visualHandshakes.values())
				visuals.put(hc.getReferencedComponent(), hc);
		}
		
		throw new NotImplementedException();
		/*for(MathConnection connection : model.getConnections()) {
			VisualConnection visualConnection = new VisualConnection(storage);
			
			VisualHandshake first = visuals.get(connection.getFirst());
			VisualHandshake second = visuals.get(connection.getSecond());
			
			visualConnection.setVisualConnectionDependencies(first, 
					second, new Polyline(visualConnection, storage), connection);
			//VisualConnection visualConnection = new VisualConnection(connection, visuals.get(connection.getFirst()), visuals.get(connection.getSecond()));
			add(visualConnection);
		}*/
	}
	/*
	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
		
		if ( ! (first instanceof VisualHandshake && second instanceof VisualHandshake) )
			throw new InvalidConnectionException("Only handshakes can be connected");
		
		((BalsaCircuit)getMathModel()).validateConnection( ((VisualHandshake)first).getHandshakeComponent(),
					((VisualHandshake)second).getHandshakeComponent() ); 
				
		
	}
	
	@Override
	public void connect(Node first, Node second)
			throws InvalidConnectionException {
		final VisualHandshake firstHandshake = (VisualHandshake)first;
		final VisualHandshake secondHandshake = (VisualHandshake)second;
		final MathConnection connect = ((BalsaCircuit)getMathModel()).connect( firstHandshake.getHandshakeComponent(),
				secondHandshake.getHandshakeComponent() );
		
		add(Hierarchy.getNearestContainer(first, second), new VisualConnection(connect, firstHandshake, secondHandshake, storage));
	}*/

	public VisualBreezeComponent createComponent(String componentName, Point2D where) {
		if(true)throw new NotImplementedException();
		BreezeComponent comp = new BreezeComponent(null); 
		DynamicComponent instance = null;
		try {
			//TODO: Instantiate a DynamicComponent
			//instance = new BreezeLibrary(BalsaSystem.DEFAULT()).get(name) balsaClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}				
		comp.setUnderlyingComponent(instance);
		return null;		
	}
}