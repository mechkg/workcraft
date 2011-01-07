package org.workcraft.relational.petrinet.declaration;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.relational.interfaces.DeletionPolicy;
import org.workcraft.relational.interfaces.Field;
import org.workcraft.relational.interfaces.ObjectDeclaration;
import org.workcraft.relational.interfaces.PrimitiveField;
import org.workcraft.relational.interfaces.Relation;
import org.workcraft.relational.interfaces.RelationField;
import org.workcraft.relational.petrinet.declaration.RelationalPetriNet.PetriNetRelations;

import pcollections.HashTreePBag;
import pcollections.HashTreePMap;
import pcollections.PMap;
import pcollections.PVector;
import pcollections.TreePVector;

public class VisualPetriNetRelations {
	
	private final PetriNetRelations mathPetriNet;
	private final Relation visualNodeRelation;
	private final Relation visualTransitionRelation;
	private final Relation visualPlaceRelation;
	private final Relation visualGroupRelation;

	public PVector<Relation> getSchema() {
		return TreePVector.<Relation>empty().
		plus(visualNodeRelation).
		plus(visualTransitionRelation).
		plus(visualPlaceRelation).
		plus(visualGroupRelation).
		plusAll(mathPetriNet.getSchema());
	}
	
	public VisualPetriNetRelations(PetriNetRelations mathPetriNet,
			Relation visualNodeRelation, Relation visualTransitionRelation,
			Relation visualPlaceRelation, Relation visualGroupRelation) {
				this.mathPetriNet = mathPetriNet;
				this.visualNodeRelation = visualNodeRelation;
				this.visualTransitionRelation = visualTransitionRelation;
				this.visualPlaceRelation = visualPlaceRelation;
				this.visualGroupRelation = visualGroupRelation;
	}

	public static VisualPetriNetRelations create(PetriNetRelations mathPetriNet) {
		ObjectDeclaration visualPlace = new ObjectDeclaration.Instance("visualPlace");
		ObjectDeclaration visualTransition = new ObjectDeclaration.Instance("visualTransition");
		
		// TODO: define visual arc relation
		//ObjectDeclaration visualArc = new ObjectDeclaration.Instance("visualArc");
		
		ObjectDeclaration visualGroup = new ObjectDeclaration.Instance("visualGroup");
		ObjectDeclaration visualNode = new ObjectDeclaration.Instance("visualNode");
		
		PMap<String, Field> visualPlaceFields = HashTreePMap.<String, Field>empty().
		plus("mathNode", new RelationField.Instance(mathPetriNet.place.getObjectDeclaration(), DeletionPolicy.CASCADE_DELETE)).
		plus("visualNode", new RelationField.Instance(visualNode, DeletionPolicy.CASCADE_DELETE)).
		plus("tokenColor", new PrimitiveField.Instance<Color>(Color.class));
		
		PMap<String, Field> visualTransitionFields = HashTreePMap.<String, Field>empty().
		plus("mathNode", new RelationField.Instance(mathPetriNet.transition.getObjectDeclaration(), DeletionPolicy.CASCADE_DELETE)).
		plus("visualNode", new RelationField.Instance(visualNode, DeletionPolicy.CASCADE_DELETE));
		
		PMap<String, Field> visualGroupFields = HashTreePMap.<String, Field>empty().
		plus("visualNode", new RelationField.Instance(visualNode, DeletionPolicy.CASCADE_DELETE));
		
		PMap<String, Field> visualNodeFields = HashTreePMap.<String, Field>empty().
		plus("parent", new RelationField.Instance(visualGroup, DeletionPolicy.CASCADE_DELETE)).
		plus("transform", PrimitiveField.Instance.create(AffineTransform.class));
		
		Relation visualNodeRelation = new Relation.Instance(visualNode, visualNodeFields, TreePVector.<PVector<String>>empty());
		Relation visualTransitionRelation = new Relation.Instance(visualTransition, visualTransitionFields, TreePVector.<PVector<String>>empty().plus(TreePVector.singleton("mathNode")));
		Relation visualPlaceRelation = new Relation.Instance(visualPlace, visualPlaceFields, TreePVector.<PVector<String>>empty().plus(TreePVector.singleton("mathNode")));
		Relation visualGroupRelation = new Relation.Instance(visualGroup, visualGroupFields, TreePVector.<PVector<String>>empty());
		return new VisualPetriNetRelations(mathPetriNet, visualNodeRelation, visualTransitionRelation, visualPlaceRelation, visualGroupRelation);
	}
}