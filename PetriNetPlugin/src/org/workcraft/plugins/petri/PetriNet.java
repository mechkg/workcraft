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

package org.workcraft.plugins.petri;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.UniqueNameReferenceManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

@VisualClass ("org.workcraft.plugins.petri.VisualPetriNet")
public class PetriNet extends AbstractMathModel implements PetriNetModel {
	
	
	final UniqueNameReferenceManager names;

	public PetriNet() {
		this(null, null);
	}

	public PetriNet(Container root) {
		this(root, null);
	}

	public PetriNet(Container root, References refs) {
		this(new ConstructionParameters(root, refs));
	}
	
	static class ConstructionParameters {
		public ConstructionParameters(Container root, References refs) {
			this.root = (root == null) ? new MathGroup() : root;
			this.referenceManager = new UniqueNameReferenceManager(this.root, refs, new Func<Node, String>() {
				@Override
				public String eval(Node arg) {
					if (arg instanceof Place)
						return "p";
					if (arg instanceof Transition)
						return "t";
					if (arg instanceof Connection)
						return "con";
					return "node";
				}
			});
		}
		
		final Container root;
		final UniqueNameReferenceManager referenceManager;
	}
	
	
	protected PetriNet(ConstructionParameters construction) {
		super(construction.root, construction.referenceManager);
		names = construction.referenceManager;
	}

	public void validate() throws ModelValidationException {
	}

	final public Place createPlace() {
		return createPlace(null);
	}

	final public Transition createTransition() {
		return createDummyTransition(null);
	}

	final public Place createPlace(String name) {
		Place newPlace = new Place();
		if (name!=null)
			setName(newPlace, name);
		getRoot().add(newPlace);
		refreshStupidObservers();
		return newPlace;
	}

	final public Transition createDummyTransition(String name) {
		Transition newTransition = new Transition();
		if (name!=null)
			setName(newTransition, name);
		getRoot().add(newTransition);
		refreshStupidObservers();
		return newTransition;
	}

	final public Collection<Place> getPlaces() {
		refreshStupidObservers();
		return Hierarchy.getDescendantsOfType(getRoot(), Place.class);
	}

	final public Collection<Transition> getTransitions() {
		refreshStupidObservers();
		return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
	}

	final public boolean isEnabled (Transition t) {
		refreshStupidObservers();
		return isEnabled (this, t);
	}

	final public static boolean isEnabled (PetriNetModel net, Transition t) {
		// gather number of connections for each pre-place
		Map<Place, Integer> map = new HashMap<Place, Integer>();
		for (Connection c: net.getConnections(t)) {
			if (c.getSecond()==t) {
				if (map.containsKey(c.getFirst())) {
					map.put((Place)c.getFirst(), map.get(c.getFirst())+1);
				} else {
					map.put((Place)c.getFirst(), 1);
				}
			}
		}
		
		for (Node n : net.getPreset(t))
			if (GlobalCache.eval(((Place)n).tokens()) < map.get((Place)n))
				return false;
		return true;
	}

	final public void fire (Transition t) {
		fire (this, t);
	}

	final public void unFire(Transition t) {
		unFire(this, t);
	}
	
	final public static void unFire(PetriNetModel net, Transition t) {
		// the opposite action to fire, no additional checks, 
		// the transition given must be correct
		// for the transition to be enabled
		
		for (Connection c : net.getConnections(t)) {
			if (t==c.getFirst()) {
				Place to = (Place)c.getSecond();
				to.tokens().setValue(eval(to.tokens())-1);
			} 
			if (t==c.getSecond()) {
				Place from = (Place)c.getFirst();
				from.tokens().setValue(eval(from.tokens())+1);
			}
		}
	}
	
	final public static void fire (PetriNetModel net, Transition t) {
		if (net.isEnabled(t))
		{
			for (Connection c : net.getConnections(t)) {
				if (t==c.getFirst()) {
					Place to = (Place)c.getSecond();
					to.tokens().setValue(eval(to.tokens())+1);
				} 
				if (t==c.getSecond()) {
					Place from = (Place)c.getFirst();
					from.tokens().setValue(eval(from.tokens())-1);
				}
			}
		}
	}

	public MathConnection connect(Node first, Node second) throws InvalidConnectionException {
		if (first instanceof Place && second instanceof Place)
			throw new InvalidConnectionException ("Connections between places are not valid");
		if (first instanceof Transition && second instanceof Transition)
			throw new InvalidConnectionException ("Connections between transitions are not valid");
		
		
		MathConnection con = new MathConnection((MathNode)first, (MathNode)second);
		
		Hierarchy.getNearestContainer(first, second).add(con);
		refreshStupidObservers();
		
		return con;
	}

	public String getName(Node n) {
		refreshStupidObservers();
		return this.names.getNodeReference(n);
	}

	public void setName(Node n, String name) {
		this.names.setName(n, name);
		refreshStupidObservers();
	}

	@Override
	public Properties getProperties(Node node) {
		refreshStupidObservers();
		return Properties.Mix.from(new NamePropertyDescriptor(this, node));
	}
	
	@Override
	public void refreshStupidObservers() {
		names.refresh();
		super.refreshStupidObservers();
	}
}