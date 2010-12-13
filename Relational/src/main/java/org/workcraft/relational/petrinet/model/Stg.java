package org.workcraft.relational.petrinet.model;

import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Map;

class Place {
	
}

class Arc {
	
}

class Transition {
	
}

class TransitionData {
	final String name;
}

class ArcData {
	final Place place;
	final Transition transition;
	final int consumed;
	final int produced;
}

class PlaceData {
	final String name;
	final int initialMarking;
}

class PetriNetData {
	
	Map<Place, PlaceData> places;
	Map<Transition, TransitionData> transitions;
	Map<Arc, ArcData> arcs;
}

class VisualNode {}
class VisualData {
	final AffineTransform transform;
}

class Group {}

class VisualPetriNetData {
	Map<VisualNode, VisualData> visualData;
	Map<Place, VisualNode> placeAsVisualNode;
	Map<Transition, VisualNode> transitionAsVisualNode;
	Map<Group, VisualNode> groupAsVisualNode;
}
