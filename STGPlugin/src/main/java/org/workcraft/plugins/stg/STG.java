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

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase.ValueHandleTuple;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionBase;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dependencymanager.util.listeners.Listener;
import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Container;
import org.workcraft.dom.ModelSpecification;
import org.workcraft.dom.Node;
import org.workcraft.dom.NodeContext;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.choice.ChoiceProperty;
import org.workcraft.gui.propertyeditor.integer.IntegerProperty;
import org.workcraft.gui.propertyeditor.string.StringProperty;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Pair;
import org.workcraft.util.Triple;

import pcollections.PVector;
import pcollections.TreePVector;

public class STG extends AbstractModel implements STGModel {
	
	private final StgRefMan referenceManager;
	private final StgTextRefMan textReferenceManager;

	private static class ConstructionInfo {
		private final StorageManager storage;
		public ConstructionInfo(Container root, References refs, StorageManager storage) {
			this.storage = storage;
			if(root == null)
				this.root = new MathGroup(storage);
			else
				this.root = root;
			this.nodeContext = createDefaultNodeContext(this.root);
			Pair<? extends StgTextRefMan, ? extends StgRefMan> p = STGReferenceManager.create(this.root, nodeContext, refs);
			this.referenceManager = p.getSecond();
			this.textReferenceManager = p.getFirst();
		}
		public final StgTextRefMan textReferenceManager;
		public final StgRefMan  referenceManager; 
		public final Expression<? extends NodeContext>  nodeContext; 
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
		super(new ModelSpecification(info.root, info.textReferenceManager.referenceManager(), createDefaultControllerChain(info.root, info.nodeContext), info.nodeContext));
		storage = info.storage;
		referenceManager = info.referenceManager;
		textReferenceManager = info.textReferenceManager;
		signalTypeConsistencySupervisor = new SignalTypeConsistencySupervisor(this);
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
			referenceManager.setName(newPlace, name);

		add(newPlace);

		return newPlace;
	}


	final public DummyTransition createDummyTransition(String name) {
		DummyTransition newTransition = new DummyTransition(storage);
		if (name!=null)
			referenceManager.setInstance(newTransition, Pair.of(name, (Integer)null));
		add(newTransition);
		return newTransition;
	}

	public final StorageManager storage;
	
	final public SignalTransition createSignalTransition(String name) {
		return createSignalTransition(name, Direction.TOGGLE);
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

	final public Collection<STGPlace> getPlaces() {
		return Hierarchy.getDescendantsOfType(getRoot(), STGPlace.class);
	}

	final public Collection<Transition> getTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
	}

	@Override
	public Collection<DummyTransition> getDummies() {
		return Hierarchy.getDescendantsOfType(getRoot(), DummyTransition.class);
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
		for (DummyTransition t : getDummies())
			result.add(eval(referenceManager.state()).getInstance(t).getFirst());
		return result;
	}

	private Set<String> getUniqueNames(Collection<SignalTransition> transitions) {
		Set<String> result = new HashSet<String>();
		for (SignalTransition st : transitions)
			result.add(GlobalCache.eval(signalName(st)));
		return result;
	}

	public ModifiableExpression<Integer> instanceNumber (final Transition t) {
		if(t instanceof SignalTransition) {
			final SignalTransition st = (SignalTransition)t;
			
			return new ModifiableExpressionBase<Integer>() {
				@Override
				public void setValue(Integer newValue) {
					StgRefManState refMan = eval(referenceManager.state());
					referenceManager.setInstance(st, Pair.of(refMan.getInstance(st).getFirst(), newValue));
				}

				@Override
				protected Integer evaluate(EvaluationContext context) {
					return context.resolve(referenceManager.state()).getInstance(st).getSecond();
				}
			};
		}
		else {
			final DummyTransition dt = (DummyTransition)t;
			
			return new ModifiableExpressionBase<Integer>() {
				@Override
				public void setValue(Integer newValue) {
					StgRefManState refMan = eval(referenceManager.state());
					referenceManager.setInstance(dt, Pair.of(refMan.getInstance(dt).getFirst(), newValue));
				}

				@Override
				protected Integer evaluate(EvaluationContext context) {
					return context.resolve(referenceManager.state()).getInstance(dt).getSecond();
				}
			};
			
		}
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

	public ModifiableExpression<String> name(final STGPlace node) {
		return new ModifiableExpressionBase<String>() {

			@Override
			public void setValue(String newValue) {
				referenceManager.setName(node, newValue);
			}

			@Override
			protected String evaluate(EvaluationContext context) {
				return context.resolve(referenceManager.state()).getName(node);
			}
		};
	}

	public ModifiableExpression<String> name(final DummyTransition dt) {
		return new ModifiableExpressionBase<String>() {

			@Override
			public void setValue(String newValue) {
				Integer newInstanceNumber = null;
				referenceManager.setInstance(dt, Pair.of(newValue, newInstanceNumber));
			}

			@Override
			protected String evaluate(EvaluationContext context) {
				return context.resolve(referenceManager.state()).getInstance(dt).getFirst();
			}
		};
	}

	@Override
	public PVector<EditableProperty> getProperties(Node node) {
		PVector<EditableProperty> superProperties = super.getProperties(node);
		if (node instanceof STGPlace) {
			if (!eval(((STGPlace) node).implicit()))
				return superProperties.plus(StringProperty.create("Name", name((STGPlace)node)));
			else
				return superProperties;
		}
		else if (node instanceof SignalTransition) {
			SignalTransition transition = (SignalTransition)node;

			final PVector<Pair<String, Direction>> directions = TreePVector.<Pair<String, Direction>>empty()
				.plus(Pair.of("+", Direction.PLUS))
				.plus(Pair.of("-", Direction.MINUS))
				.plus(Pair.of("~", Direction.TOGGLE));
			
			final PVector<Pair<String, Type>> signalTypes = TreePVector.<Pair<String, Type>>empty()
				.plus(Pair.of("Input", Type.INPUT))
				.plus(Pair.of("Output", Type.OUTPUT))
				.plus(Pair.of("Internal", Type.INTERNAL));
		
			return superProperties
				.plus(StringProperty.create("Signal name", signalName(transition)))
				.plus(ChoiceProperty.create("Transition direction", directions, direction(transition)))
				.plus(IntegerProperty.create("Instance number", instanceNumber(transition)))
				.plus(ChoiceProperty.create("Signal type", signalTypes, signalType(transition)))
				;
		} 
		else if (node instanceof DummyTransition) {
			DummyTransition dummy = (DummyTransition)node;
			return superProperties
				.plus(StringProperty.create("Name", name(dummy)))
				.plus(IntegerProperty.create("Instance", instanceNumber(dummy)))
				;
		}
		else 
			return superProperties;
	}

	public ModifiableExpression<String> signalName(final SignalTransition transition) {
		return new ModifiableExpressionBase<String>() {

			@Override
			protected String evaluate(EvaluationContext context) {
				Pair<Pair<String, Direction>, Integer> instance = context.resolve(referenceManager.state()).getInstance(transition);
				Pair<String, Direction> signalEdge = instance.getFirst();
				return signalEdge.getFirst();
			}

			@Override
			public void setValue(String newValue) {
				StgRefManState refMan = eval(referenceManager.state());
				Pair<Pair<String, Direction>, Integer> oldInstance = refMan.getInstance(transition);
				referenceManager.setInstance(transition, Pair.of(Pair.of(newValue, oldInstance.getFirst().getSecond()), (Integer)null));
				signalTypeConsistencySupervisor.nameChanged(transition, oldInstance.getFirst().getFirst(), newValue);
			}
		};
	}

	public ModifiableExpression<SignalTransition.Type> signalType(final SignalTransition transition) {
		return new ModifiableExpression<SignalTransition.Type>() {

			@Override
			public ValueHandleTuple<? extends SignalTransition.Type> getValue(Listener subscriber) {
				return transition.signalType().getValue(subscriber);
			}

			@Override
			public void setValue(SignalTransition.Type newValue) {
				SignalTransition.Type oldValue = eval(this);
				transition.signalType().setValue(newValue);
				signalTypeConsistencySupervisor.signalTypeChanged(transition, oldValue, newValue);
			}
		};
	}

	public Collection<SignalTransition> getSignalTransitions(String signalName) {
		Collection<SignalTransition> allTransitions = getSignalTransitions();
		ArrayList<SignalTransition> result = new ArrayList<SignalTransition>();
		for(SignalTransition signal : allTransitions) {
			if(eval(signalName(signal)).equals(signalName))
				result.add(signal);
		}
		return result;
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
	
	public void makeExplicit(STGPlace implicitPlace) {
		implicitPlace.implicit().setValue(false);
		referenceManager.setName(implicitPlace, null);
	}

	public DummyTransition createDummyTransition() {
		return createDummyTransition(null);
	}

	public ModifiableExpression<Direction> direction(final SignalTransition transition) {
		return new ModifiableExpressionBase<SignalTransition.Direction>() {

			@Override
			public void setValue(Direction newValue) {
				StgRefManState refMan = eval(referenceManager.state());
				Pair<Pair<String, Direction>, Integer> oldInstance = refMan.getInstance(transition);
				referenceManager.setInstance(transition, Pair.of(Pair.of(oldInstance.getFirst().getFirst(), newValue), (Integer)null));
			}

			@Override
			protected Direction evaluate(EvaluationContext context) {
				return context.resolve(referenceManager.state()).getInstance(transition).getFirst().getSecond();
			}
		};
		
	}

	public void setName(MathNode node, String newName) {
		if(node instanceof SignalTransition) {
			// not very good way to do it. chaining StgRefMan would be better.
			// However, Signals should be made real objects instead of imaginary anyway, so we'll live with this hack for a while
			SignalTransition trans = (SignalTransition)node;
			ModifiableExpression<String> signalNameExpr = signalName(trans);
			String oldTransName = eval(signalNameExpr);
			textReferenceManager.setName(node, newName);
			String newTransName = eval(signalNameExpr);
			signalTypeConsistencySupervisor.nameChanged(trans, oldTransName, newTransName);
		}
		else
			textReferenceManager.setName(node, newName);
	}

	public SignalTransition createSignalTransition(String name, Direction direction) {
		SignalTransition ret = new SignalTransition(storage);
		if (name!=null)
			referenceManager.setInstance(ret, Pair.of(Pair.of(name, direction), (Integer)null));
		add(ret);
		return ret;
	}

}