package org.workcraft.plugins.petri.tools;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;
import static org.workcraft.util.Maybe.Util.*;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.util.Function;
import org.workcraft.util.Maybe;

public class PetriNetSpecific {
	public static Function<Node, Maybe<String>> nodeEventExtractor(final PetriNetModel pn) {
		return new Function<Node, Maybe<String>>() {
			@Override
			public Maybe<String> apply(Node node) {
				if (node instanceof VisualTransition) {
					Transition transition = ((VisualTransition) node).getReferencedTransition();

					return just(eval(pn.referenceManager()).getNodeReference(transition));
				} else
					return nothing();
			}
		};
	}
	
	public static void applyMarking(final PetriNetModel net, Map<Place, Integer> marking) {
		for (Place p : marking.keySet()) {
			if (net.getPlaces().contains(p)) {
				p.tokens().setValue(marking.get(p));
			} else {
				// ExceptionDialog.show(null, new
				// RuntimeException("Place "+p.toString()+" is not in the model"));
			}
		}
	}

	public static Map<Place, Integer> readMarking(final PetriNetModel net) {
		HashMap<Place, Integer> result = new HashMap<Place, Integer>();
		for (Place p : net.getPlaces()) {
			result.put(p, eval(p.tokens()));
		}
		return result;
	}
	
	public static SimulationModel<String, Map<Place, Integer>> petriNetAsSimulationModel (final PetriNetModel net) {
		return new SimulationModel<String, Map<Place, Integer>>() {

			@Override
			public boolean canFire(String event) {
				Transition t = transitionByName(event);
				return t!=null && net.isEnabled(t);
			}

			@Override
			public void fire(String event) {
				Transition t = transitionByName(event);
				if(t!=null)
					net.fire(t);
			}

			private Transition transitionByName(String event) {
				Node node = eval(net.referenceManager()).getNodeByReference(event);
				return node instanceof Transition ? (Transition)node : null;
			}

			@Override
			public boolean canUnfire(String event) {
				Transition t = transitionByName(event);
				return t!=null && net.isUnfireEnabled(t);
			}

			@Override
			public void unfire(String event) {
				Transition t = transitionByName(event);
				if(t!=null)
					net.unFire(t);
			}

			@Override
			public Map<Place, Integer> saveState() {
				return readMarking(net);
			}

			@Override
			public void loadState(Map<Place, Integer> marking) {
				applyMarking(net, marking);
			}
		};
	}
}
