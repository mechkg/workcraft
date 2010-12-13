package org.workcraft.relational.petrinet.declaration;


import org.workcraft.relational.interfaces.DeletionPolicy;
import org.workcraft.relational.interfaces.Field;
import org.workcraft.relational.interfaces.ObjectDeclaration;
import org.workcraft.relational.interfaces.PrimitiveField;
import org.workcraft.relational.interfaces.Relation;
import org.workcraft.relational.interfaces.RelationField;

import pcollections.HashTreePMap;
import pcollections.PMap;
import pcollections.PVector;
import pcollections.TreePVector;

public class RelationalPetriNet {
	
	public static VisualPetriNetRelations createSchema() {
		return VisualPetriNetRelations.create(PetriNetRelations.create());
	}
	
	public static class PetriNetRelations {
		static PetriNetRelations create() {
			
			ObjectDeclaration place = new ObjectDeclaration.Instance("place"); 
			ObjectDeclaration transition = new ObjectDeclaration.Instance("transition"); 
			ObjectDeclaration arc = new ObjectDeclaration.Instance("arc");
			
			PMap<String, Field> placeFields = HashTreePMap.<String, Field>empty().
			plus("initialMarking", PrimitiveField.Instance.create(Integer.class)).
			plus("name", PrimitiveField.Instance.create(String.class));
			
			PMap<String, Field> transitionFields = HashTreePMap.<String, Field>empty().
			plus("name", PrimitiveField.Instance.create(String.class));
			
			PMap<String, Field> arcFields = HashTreePMap.<String, Field>empty().
			plus("place", new RelationField.Instance(place, DeletionPolicy.CASCADE_DELETE)).
			plus("transition", new RelationField.Instance(transition, DeletionPolicy.CASCADE_DELETE)).
			plus("consumed", PrimitiveField.Instance.create(Integer.class)).
			plus("produced", PrimitiveField.Instance.create(Integer.class));
			
			PVector<PVector<String>> transitionUniqueKeys = TreePVector.<PVector<String>>empty().
			plus(TreePVector.singleton("name"));
			
			PVector<PVector<String>> placeUniqueKeys = TreePVector.<PVector<String>>empty().
			plus(TreePVector.singleton("name"));
			
			PVector<PVector<String>> arcUniqueKeys = TreePVector.<PVector<String>>empty().
			plus(TreePVector.singleton("place").plus("transition"));
			
			Relation placeRelation = new Relation.Instance(place, placeFields, placeUniqueKeys);
			Relation transitionRelation = new Relation.Instance(transition, transitionFields, transitionUniqueKeys);
			Relation arcRelation = new Relation.Instance(arc, arcFields, arcUniqueKeys);
			
			return new PetriNetRelations(placeRelation, transitionRelation, arcRelation);
		}
		
		public final Relation place;
		public final Relation transition;
		public final Relation arc;
		
		public PetriNetRelations(
				Relation place,
				Relation transition,
				Relation arc){
			this.place = place;
			this.transition = transition;
			this.arc = arc;
		}

		public PVector<? extends Relation> getSchema() {
			return TreePVector.<Relation>empty().
			plus(place).
			plus(transition).
			plus(arc);
		}
	}
}
