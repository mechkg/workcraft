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

package org.workcraft.plugins.circuit;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.interop.ServiceHandle;
import org.workcraft.plugins.circuit.Contact.IoType;
import org.workcraft.util.Hierarchy;

public class VisualCircuit extends AbstractVisualModel {

	public static ServiceHandle<VisualCircuit> SERVICE_HANDLE = ServiceHandle.createNewService(VisualCircuit.class, "Digital Circuit");
	
	private Circuit circuit;
	
	@Override
	public void validateConnection(Node first, Node second)	throws InvalidConnectionException {
		if (first==second) {
			throw new InvalidConnectionException ("Connections are only valid between different objects");
		}
		
		if (first instanceof VisualCircuitConnection || second instanceof VisualCircuitConnection) {
			throw new InvalidConnectionException ("Connecting with connections is not implemented yet");
		}
		if (first instanceof VisualComponent && second instanceof VisualComponent) {
			
			
			for (Connection c: eval(nodeContext()).getConnections(second)) {
				if (c.getSecond()==second)
					throw new InvalidConnectionException ("Only one connection is allowed as a driver");
			}
			
			if (second instanceof VisualContact) {
				Node toParent = eval(((VisualComponent)second).parent());
				Contact.IoType toType = eval(((Contact)((VisualComponent)second).getReferencedComponent()).ioType());
				
				if ((toParent instanceof VisualCircuitComponent) && toType == Contact.IoType.OUTPUT)
					throw new InvalidConnectionException ("Outputs of the components cannot be driven");

				if (!(toParent instanceof VisualCircuitComponent) && toType == Contact.IoType.INPUT)
					throw new InvalidConnectionException ("Inputs from the environment cannot be driven");
			}
		}
	}
	
/*	
	private final class StateSupervisorExtension extends StateSupervisor {
		@Override
		public void handleEvent(StateEvent e) {
//			if(e instanceof PropertyChangedEvent)
				
		}
	}
*/

	public VisualCircuit(Circuit model, VisualGroup root, StorageManager storage)
	{
		super(model, root, storage);
		circuit=model;
	}

	public VisualCircuit(Circuit model, StorageManager storage)
	throws VisualModelInstantiationException {
		super(model, storage);
		circuit=model;
		try {
			createDefaultFlatStructure();
		} catch (NodeCreationException e) {
			throw new VisualModelInstantiationException(e);
		}
		
		//new StateSupervisorExtension().attach(getRoot());
	}

	@Override
	public void connect(Node first, Node second)
			throws InvalidConnectionException {
		validateConnection(first, second);
		
		if (first instanceof VisualComponent && second instanceof VisualComponent) {
			VisualComponent c1 = (VisualComponent) first;
			VisualComponent c2 = (VisualComponent) second;
			MathConnection con = (MathConnection) circuit.connect(c1.getReferencedComponent(), c2.getReferencedComponent());
			VisualCircuitConnection ret = new VisualCircuitConnection(con, c1, c2, storage);
			add(Hierarchy.getNearestContainer(c1, c2), ret);
		}
	}
	
	@Override
	public Properties getProperties(Node node) {
		if(node instanceof VisualFunctionContact)
		{
			VisualFunctionContact contact = (VisualFunctionContact)node;
			VisualContactFormulaProperties props = new VisualContactFormulaProperties(this);
			return Properties.Merge.add(super.getProperties(node),
					props.getSetProperty(contact),
					props.getResetProperty(contact));
		}
		else return super.getProperties(node);
	}

	public VisualFunctionContact  getOrCreateOutput(String name, double x, double y) {
		
		for(VisualFunctionContact c : Hierarchy.filterNodesByType(eval(getRoot().children()), VisualFunctionContact.class)) {
			if(eval(c.name()).equals(name)) return c;
		}
		
		return createFunctionContact(IoType.OUTPUT, new Point2D.Double(x,y));
		
	}

	public VisualFunctionContact createFunctionContact(IoType ioType, Point2D point) {
		return createFunctionContact(ioType, point, "");
	}
	
	public VisualFunctionContact createFunctionContact(IoType ioType, Point2D point, String name) {
		FunctionContact fc = new FunctionContact(IoType.OUTPUT, name, storage);
		VisualFunctionContact vc = new VisualFunctionContact(fc, storage);
		vc.position().setValue(point);
		circuit.add(fc);
		this.add(vc);
		
		return vc;
	}

	public VisualJoint createJoint(Point2D where) {
		Joint joint = new Joint(storage);
		VisualJoint visualJoint = new VisualJoint(joint, storage);
		visualJoint.position().setValue(where);
		circuit.add(joint);
		this.add(visualJoint);
		
		return visualJoint;
	}

	public VisualFunctionComponent createFunctionComponent(Point2D where) {
		FunctionComponent functionComponent = new FunctionComponent(storage);
		VisualFunctionComponent visualFunctionComponent = new VisualFunctionComponent(functionComponent, storage);
		visualFunctionComponent.position().setValue(where);
		circuit.add(functionComponent);
		this.add(visualFunctionComponent);
		
		return visualFunctionComponent;
	}

}
