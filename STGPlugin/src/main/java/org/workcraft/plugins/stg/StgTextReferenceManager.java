package org.workcraft.plugins.stg;

import java.util.Set;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.Node;
import org.workcraft.dom.NodeContext;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.DuplicateIDException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Identifier;
import org.workcraft.util.Pair;
import org.workcraft.util.SetUtils;
import org.workcraft.util.Triple;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

/**
 * Implements the manager of symbolic names of STG components 
 * by converting those names to/from strongly-typed keys stored in a provided @link{StgRefMan}.
 */
public class StgTextReferenceManager implements StgTextRefMan {

	private final Expression<? extends NodeContext> nodeContext;
	private final StgRefMan stgRefMan;
	private final Expression<? extends ReferenceManager> referenceManagerImpl;

	public StgTextReferenceManager(Node root, Expression<? extends NodeContext> nodeContext, StgRefMan refMan, References existingReferences) {
		this.nodeContext = nodeContext;
		this.stgRefMan = refMan;
		this.referenceManagerImpl = new StgTextRefManReferenceManagerImplementation();
		
		if (existingReferences != null) {
			setExistingReference(refMan, existingReferences, root);
			for (Node n: Hierarchy.getDescendantsOfType(root, Node.class))
				setExistingReference(refMan, existingReferences, n);
			existingReferences = null;
		}
	}

	private void setExistingReference(StgRefMan refMan, References existingReferences, Node n) {
		final String reference = existingReferences.getReference(n);
		if (reference != null)
			setName (refMan, n, reference);
	}


	@Override
	public void setName(Node node, String s) {
		StgRefMan refMan = stgRefMan;
		StgTextReferenceManager.setName(refMan, node, s);
	}
	
	private static void setName(StgRefMan refMan, Node node, String s) {
		if (node instanceof SignalTransition) {
			final SignalTransition st = (SignalTransition)node;

			try {
				final Triple<String, Direction, Integer> r = LabelParser.parseFull(s);
				if (r==null)
					throw new ArgumentException (s + " is not a valid signal transition label");

				refMan.setInstance(st, Pair.of(Pair.of(r.getFirst(), r.getSecond()), r.getThird()));

			} catch (DuplicateIDException e) {
				throw new ArgumentException ("Instance number " + e.getId() + " is already taken.");
			} catch (ArgumentException e) {
				if (Identifier.isValid(s)) {
					refMan.setInstance(st, Pair.of(Pair.of(s, eval(refMan.state()).getInstance(st).getFirst().getSecond()), (Integer)null));
				} else
					throw new ArgumentException ("\"" + s + "\" is not a valid signal transition label.");
			}
		} else if (node instanceof DummyTransition) {
			final DummyTransition dt = (DummyTransition)node;

			try {
				final Pair<String,Integer> r = LabelParser.parse(s);
				if (r==null)
					throw new ArgumentException (s + " is not a valid transition label");
				refMan.setInstance(dt, r);
			} catch (DuplicateIDException e) {
				throw new ArgumentException ("Instance number " + e.getId() + " is already taken.");
			}
		}
		else if(node instanceof STGPlace) {
			if(!GlobalCache.eval(((STGPlace)node).implicit()))
				refMan.setName((STGPlace)node, s);
		}
		else
			refMan.setMiscNodeName(node, s);
	}
	
	
	class StgTextRefManReferenceManagerImplementation extends ExpressionBase<ReferenceManager> {
	@Override
	protected ReferenceManager evaluate(final EvaluationContext context) {
		return new ReferenceManager(){
			
			private String getStgPlaceReference(STGPlace place) {
				if(context.resolve(place.implicit())) {
					Set<Node> preset = context.resolve(nodeContext).getPreset(place);
					Set<Node> postset = context.resolve(nodeContext).getPostset(place);
					
					if (!(preset.size()==1 && postset.size()==1))
						throw new RuntimeException ("An implicit place cannot have more that one transition in its preset or postset.");
					
					return "<"+getNodeReference(preset.iterator().next()) 
								+ "," + getNodeReference(postset.iterator().next()) + ">";
				} else
					return context.resolve(stgRefMan.state()).getName(place);
			}

			private String getSignalTransitionReference(SignalTransition st) {
				Pair<Pair<String, Direction>, Integer> instance2 = context.resolve(stgRefMan.state()).getInstance(st);
				final Integer instance = instance2.getSecond();
				Pair<String, Direction> nameAndDirection = instance2.getFirst();
				String nameDirectionText = nameAndDirection.getFirst() + nameAndDirection.getSecond(); 
				if (instance == 0)
					return nameDirectionText;
				else
					return nameDirectionText + "/" + instance2.getSecond();
			}

			@Override
			public String getNodeReference(Node node) {
				if (node instanceof SignalTransition) {
					return getSignalTransitionReference((SignalTransition)node);
				} else if (node instanceof DummyTransition) {
					return getDummyTransitionReference(context, (DummyTransition)node);
				} else if(node instanceof STGPlace)
					return getStgPlaceReference((STGPlace)node);
				else
					return context.resolve(stgRefMan.state()).getMiscNodeName(node);
			}

			private String getDummyTransitionReference(
					final EvaluationContext context, final DummyTransition t) {
				final Pair<String, Integer> name = context.resolve(stgRefMan.state()).getInstance(t);

				if (name.getSecond() == 0)
					return name.getFirst();
				else
					return name.getFirst() + "/" + name.getSecond();
			}
			

			@Override
			public Node getNodeByReference(String reference) {
				Pair<String, String> implicitPlaceTransitions = LabelParser.parseImplicitPlaceReference(reference);
				if (implicitPlaceTransitions!=null) {

					Node t1 = getNodeByReference(implicitPlaceTransitions.getFirst());
					Node t2 = getNodeByReference(implicitPlaceTransitions.getSecond());

					Set<Node> implicitPlaceCandidates = SetUtils.intersection(context.resolve(nodeContext).getPreset(t2), context.resolve(nodeContext).getPostset(t1));

					for (Node node : implicitPlaceCandidates) {
						if (node instanceof STGPlace) {
							if (context.resolve(((STGPlace) node).implicit()))
								return node;
						}
					}

					throw new NotFoundException("Implicit place between " + implicitPlaceTransitions.getFirst() +
							" and " + implicitPlaceTransitions.getSecond() + " does not exist.");
				}	else {
				 			
					Triple<String, Direction, Integer> instancedSignalName = LabelParser.parseFull(reference);
					if (instancedSignalName != null) {
						if (instancedSignalName.getThird() == null)
							instancedSignalName = Triple.of(instancedSignalName.getFirst(), instancedSignalName.getSecond(), 0);
						return context.resolve(stgRefMan.state()).getSignalTransition(Pair.of(Pair.of(instancedSignalName.getFirst(), instancedSignalName.getSecond()), instancedSignalName.getThird()));
					}
					
					Pair<String, Integer> instancedName = LabelParser.parse(reference);
					if (instancedName != null) {
						if (instancedName.getSecond() == null)
							instancedName = Pair.of(instancedName.getFirst(), 0);
						Node n = context.resolve(stgRefMan.state()).getDummyTransition(instancedName);
						if (n!=null)
							return n;
					}
	
					return context.resolve(stgRefMan.state()).getMiscNode(reference);
				}
			}
		};

	}
	}

	@Override
	public Expression<? extends ReferenceManager> referenceManager() {
		return referenceManagerImpl;
	}

}
