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

package org.workcraft.plugins.stg;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.propertydescriptors.DummyNamePropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.InstancePropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.NamePropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.SignalNamePropertyDescriptor;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Pair;
import org.workcraft.util.SetUtils;
import org.workcraft.util.Triple;

public class STG extends AbstractModel implements STGModel {
	
	private final STGReferenceManager referenceManager;

	private static class ConstructionInfo {
		private final StorageManager storage;
		public ConstructionInfo(Container root, References refs, StorageManager storage) {
			this.storage = storage;
			if(root == null)
				this.root = new MathGroup(storage);
			else
				this.root = root;
			this.referenceManager = new STGReferenceManager(this.root, refs);
		}
		public final STGReferenceManager referenceManager; 
		public final Container root; 
	}
	
	public STG(StorageManager storage) {
		this((Container)null, storage);
	}

	public STG(Container root, StorageManager storage) {
		this (root, null, storage);
	}

	public STG(Container root, References refs, StorageManager storage) {
		this(new ConstructionInfo(root, refs, storage));
	}
	
	public STG(ConstructionInfo info) {
		super(createDefaultModelSpecification(info.root, info.referenceManager));
		storage = info.storage;
		referenceManager = info.referenceManager;
		signalTypeConsistencySupervisor = new SignalTypeConsistencySupervisor(this, info.root);
	}

	private final SignalTypeConsistencySupervisor signalTypeConsistencySupervisor;
	
	final public STGPlace createPlace() {
		return createPlace(null);
	}

	final public Transition createTransition() {
		return createDummyTransition();
	}

	final public SignalTransition createSignalTransition() {
		return createSignalTransition(null);
	}

	final public STGPlace createPlace(String name) {
		return createPlace (name, false);	
	}

	final public STGPlace createPlace(String name, boolean markAsImplicit) {
		STGPlace newPlace = new STGPlace(storage);

		newPlace.implicit().setValue(markAsImplicit);

		if (name!=null)
			setName(newPlace, name);

		add(newPlace);

		return newPlace;
	}


	final public DummyTransition createDummyTransition(String name) {
		DummyTransition newTransition = new DummyTransition(storage);
		if (name!=null)
			setName(newTransition, name);
		add(newTransition);
		return newTransition;
	}

	public final StorageManager storage;
	
	final public SignalTransition createSignalTransition(String name) {
		SignalTransition ret = new SignalTransition(storage);
		if (name!=null)
			setName(ret, name);
		add(ret);
		return ret;
	}

	public boolean isEnabled(Transition t) {
		return PetriNet.isEnabled(this, t);
	}
	
	public boolean isUnfireEnabled(Transition t) {
		return PetriNet.isUnfireEnabled(this, t);
	}

	final public void fire (Transition t) {
		PetriNet.fire(this, t);
	}

	final public void unFire (Transition t) {
		PetriNet.unFire(this, t);
	}
	
	final public Collection<SignalTransition> getSignalTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), SignalTransition.class);
	}

	final public Collection<Place> getPlaces() {
		return Hierarchy.getDescendantsOfType(getRoot(), Place.class);
	}

	final public Collection<Transition> getTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
	}

	@Override
	public Collection<Transition> getDummies() {
		return Hierarchy.getDescendantsOfType(getRoot(), Transition.class, new Func<Transition, Boolean>(){
			@Override
			public Boolean eval(Transition arg) {
				if (arg instanceof SignalTransition)
					return false;
				return true;
			}
		});
	}

	public Collection<SignalTransition> getSignalTransitions(final Type t) {
		return Hierarchy.getDescendantsOfType(getRoot(), SignalTransition.class, new Func<SignalTransition, Boolean>(){
			@Override
			public Boolean eval(SignalTransition arg) {
				return GlobalCache.eval(arg.signalType()) == t;
			}}
		);
	}

	public Set<String> getSignalNames(Type type) {
		return getUniqueNames(getSignalTransitions(type));
	}

	public Set<String> getDummyNames() {
		Set<String> result = new HashSet<String>();
		for (Transition t : getDummies())
			result.add(referenceManager.getNamePair(t).getFirst());
		return result;
	}

	private Set<String> getUniqueNames(Collection<SignalTransition> transitions) {
		Set<String> result = new HashSet<String>();
		for (SignalTransition st : transitions)
			result.add(GlobalCache.eval(st.signalName()));
		return result;
	}

	public int getInstanceNumber (Node st) {
		return referenceManager.getInstanceNumber(st);
	}
	
	public void setInstanceNumber (Node st, int number) {
		referenceManager.setInstanceNumber(st, number);
	}

	public String makeReference (Pair<String, Integer> label) {
		String name = label.getFirst();
		Integer instance = label.getSecond();
		return name+"/"+((instance==null)?0:instance);
	}
	
	public String makeReference (Triple<String, Direction, Integer> label) {
		String name = label.getFirst();
		Integer instance = label.getThird();
		return name+label.getSecond()+"/"+((instance==null)?0:instance);
	}

	public String getName(Node node) {
		ensureConsistency();
		return referenceManager.getName(node);
	}

	public void setName(Node node, String name) {
		referenceManager.setName(node, name);
	}

	@Override
	public Properties getProperties(Node node) {
		Properties.Mix result = new Properties.Mix();
		if (node instanceof STGPlace) {
			if (!eval(((STGPlace) node).implicit()))
				result.add (new NamePropertyDescriptor(this, node));
		}
		if (node instanceof SignalTransition) {
			result.add(new SignalNamePropertyDescriptor(this, (SignalTransition) node));
			result.add(new InstancePropertyDescriptor(this, node));
		} if (node instanceof DummyTransition) {
			result.add(new DummyNamePropertyDescriptor(this, (DummyTransition) node));
			result.add(new InstancePropertyDescriptor(this, node));
		}
		return result;
	}

	public Collection<SignalTransition> getSignalTransitions(String signalName) {
		return referenceManager.getSignalTransitions(signalName);
	}

	public ConnectionResult connect(Node first, Node second) throws InvalidConnectionException {
		if (first instanceof Transition && second instanceof Transition) {
			STGPlace p = new STGPlace(storage);
			p.implicit().setValue(true);

			MathConnection con1 = new MathConnection ( (Transition) first, p, storage);
			MathConnection con2 = new MathConnection ( p, (Transition) second, storage);

			Hierarchy.getNearestContainer(first, second).add( Arrays.asList(new Node[] { p, con1, con2}) );

			return new ComplexResult(p, con1, con2);
		} else if (first instanceof Place && second instanceof Place)
			throw new InvalidConnectionException ("Connections between places are not valid");
		else {
			return new SimpleResult(simpleConnect((MathNode) first, (MathNode) second));
		}
	}

	public MathConnection connect(Place first, Transition second) {
		return simpleConnect(first, second);
	}

	private MathConnection simpleConnect(MathNode first, MathNode second) {
		MathConnection con = new MathConnection(first, second, storage);
		Hierarchy.getNearestContainer(first, second).add(con);
		return con;
	}

	@Override
	public String getNodeReference(Node node) {
		ensureConsistency();
		if(node instanceof STGPlace)
		{
			if(eval(((STGPlace) node).implicit())) {
				Set<Node> preset = getNodeContext().getPreset(node);
				Set<Node> postset = getNodeContext().getPostset(node);
				
				if (!(preset.size()==1 && postset.size()==1))
					throw new RuntimeException ("An implicit place cannot have more that one transition in its preset or postset.");

				return "<"+referenceManager.getNodeReference(preset.iterator().next()) 
							+ "," + referenceManager.getNodeReference(postset.iterator().next()) + ">";
			} else
				return referenceManager.getNodeReference(node);
		} else
			return referenceManager.getNodeReference(node);
	}

	@Override
	public Node getNodeByReference(String reference) {
		Pair<String, String> implicitPlaceTransitions = LabelParser.parseImplicitPlaceReference(reference);
		if (implicitPlaceTransitions!=null) {

			Node t1 = referenceManager.getNodeByReference(implicitPlaceTransitions.getFirst());
			Node t2 = referenceManager.getNodeByReference(implicitPlaceTransitions.getSecond());

			Set<Node> implicitPlaceCandidates = SetUtils.intersection(getNodeContext().getPreset(t2), getNodeContext().getPostset(t1));

			for (Node node : implicitPlaceCandidates) {
				if (node instanceof STGPlace) {
					if (eval(((STGPlace) node).implicit()))
						return node;						
				}
			}

			throw new NotFoundException("Implicit place between " + implicitPlaceTransitions.getFirst() +
					" and " + implicitPlaceTransitions.getSecond() + " does not exist.");
		}	else
			return referenceManager.getNodeByReference(reference); 			
	}

	public void makeExplicit(STGPlace implicitPlace) {
		implicitPlace.implicit().setValue(false);
		referenceManager.setDefaultNameIfUnnamed(implicitPlace);
	}
	
	@Override
	public void ensureConsistency() {
		super.ensureConsistency();
		signalTypeConsistencySupervisor.refresh();
	}

	public DummyTransition createDummyTransition() {
		return createDummyTransition(null);
	}

}