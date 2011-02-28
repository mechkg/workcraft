package org.workcraft.plugins.stg;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.NodeContext;
import org.workcraft.dom.references.UniqueNameManager;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.observation.HierarchyObservingState;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Pair;

public class STGReferenceManager implements StgRefMan, HierarchyObservingState<StgRefManState> {
	private final InstanceManager<Pair<String,Direction>, SignalTransition> signalInstanceManager;
	private final InstanceManager<String, DummyTransition> dummyInstanceManager;
	private final UniqueNameManager<Node> defaultNameManager;

	private int signalCounter = 0;
	private int dummyCounter = 0;
	private HierarchySupervisor<StgRefManState> stateExpr;

	public STGReferenceManager() {
		this.defaultNameManager = new UniqueNameManager<Node>(new Func<Node, String>() {
			@Override
			public String eval(Node arg) {
				if (arg instanceof STGPlace)
					return "p";
				if (arg instanceof Connection)
					return "con";
				if (arg instanceof Container)
					return "group";
				return "node";
			}
		});

		this.signalInstanceManager = InstanceManager.create(new Func<SignalTransition, Pair<String,Direction>>() {
			@Override
			public Pair<String,Direction> eval(SignalTransition arg) {
				throw new NotSupportedException();
				//return Pair.of(GlobalCache.eval(arg.signalName()),  GlobalCache.eval(arg.direction()));
			}
		});
		this.dummyInstanceManager = InstanceManager.create(new Func<DummyTransition, String>() {
			@Override
			public String eval(DummyTransition arg) {
				throw new NotSupportedException();
				//return GlobalCache.eval((arg).name());
			}
		});
	}
	
	public void startHierarchySupervision(Node root) {
		stateExpr = new HierarchySupervisor<StgRefManState>(root, this);
	}

	private void nodeAdded(Node node) {
		setDefaultNameIfUnnamed(node);
		for (Node n : Hierarchy.getDescendantsOfType(node, Node.class))
			setDefaultNameIfUnnamed(n);
	}
	
	private void nodeRemoved(Node node) {
		nodeRemovedInternal(node);
		for (Node n : Hierarchy.getDescendantsOfType(node, Node.class))
			nodeRemovedInternal(n);
	}

	private void setDefaultNameIfUnnamed(Node node) {
		if (node instanceof SignalTransition) {
			final SignalTransition st = (SignalTransition)node;

			if (signalInstanceManager.contains(st))
				return;

			String name = "signal" + signalCounter++;
			signalInstanceManager.assign(st, Pair.of(name, Direction.TOGGLE));
		} else if (node instanceof DummyTransition) {
			final DummyTransition dt = (DummyTransition)node;

			if (dummyInstanceManager.contains(dt))
				return;

			String name = "dummy" + dummyCounter++;

			dummyInstanceManager.assign(dt);
		} else if (node instanceof STGPlace) {
			if (!eval(((STGPlace) node).implicit()))
				defaultNameManager.setDefaultNameIfUnnamed(node);
		}
		else
			defaultNameManager.setDefaultNameIfUnnamed(node);
	}

	private void nodeRemovedInternal (Node node) {
		if (node instanceof SignalTransition) {
			signalInstanceManager.remove((SignalTransition)node);
		} 
		if (node instanceof DummyTransition) {
			final DummyTransition dt = (DummyTransition)node;
			dummyInstanceManager.remove(dt);
		} else
			defaultNameManager.remove(node);
	}

	@Override
	public void handleEvent(Collection<? extends Node> added, Collection<? extends Node> removed) {
		for(Node node : removed)
			nodeRemoved(node);
		for(Node node : added)
			nodeAdded(node);
	}

	
	@Override
	public void setName(STGPlace place, String name) {
		if(name != null)
			defaultNameManager.setName(place, name);
		else
			defaultNameManager.setDefaultNameIfUnnamed(place);
		invalidate();
	}
	
	@Override
	public void setInstance(SignalTransition st, Pair<Pair<String, Direction>, Integer> instance) {
		signalInstanceManager.assign(st, instance);
		invalidate();
	}
	
	@Override
	public void setInstance(DummyTransition dt, Pair<String, Integer> instance) {
		dummyInstanceManager.assign(dt, instance);
		invalidate();
	}

	@Override
	public void setMiscNodeName(Node node, String s) {
		defaultNameManager.setName(node, s);
		invalidate();
	}	
	
	private void invalidate() {
		if(stateExpr != null)
			stateExpr.refresh();
	}
	
	@Override
	public Expression<StgRefManState> state() {
		return stateExpr;
	}

	@Override
	public StgRefManState getState() {
		return new StgRefManState() {
			
			@Override
			public String getName(STGPlace place) {
				return defaultNameManager.getName(place);
			}
			
			@Override
			public Pair<Pair<String, Direction>, Integer> getInstance(SignalTransition st) {
				return signalInstanceManager.getInstance(st);
			}
			
			@Override
			public Pair<String, Integer> getInstance(DummyTransition dt) {
				return dummyInstanceManager.getInstance(dt);
			}

			@Override
			public String getMiscNodeName(Node node) {
				return defaultNameManager.getName(node);
			}

			@Override
			public DummyTransition getDummyTransition(Pair<String, Integer> instance) {
				return dummyInstanceManager.getObject(instance);
			}

			@Override
			public SignalTransition getSignalTransition(Pair<Pair<String, Direction>, Integer> instance) {
				return signalInstanceManager.getObject(instance);
			}

			@Override
			public STGPlace getPlace(String name) {
				return (STGPlace)defaultNameManager.get(name);
			}

			@Override
			public Node getMiscNode(String name) {
				return defaultNameManager.get(name);
			}
		};
	}

	public static Pair<? extends StgTextRefMan, ? extends StgRefMan> create(Node root, Expression<? extends NodeContext> nodeContext, References refs) {
		final STGReferenceManager rm = new STGReferenceManager();
		Pair<StgTextReferenceManager, STGReferenceManager> result = Pair.of(new StgTextReferenceManager(root, nodeContext, rm, refs), rm);
		rm.startHierarchySupervision(root);
		return result;
	}
}