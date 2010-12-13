package org.workcraft.relational.petrinet.declaration;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.relational.interfaces.DeletionPolicy;
import org.workcraft.relational.interfaces.Field;
import org.workcraft.relational.interfaces.ObjectDeclaration;
import org.workcraft.relational.interfaces.Relation;
import org.workcraft.relational.interfaces.RelationField;
import org.workcraft.relational.petrinet.declaration.RelationalPetriNet.PetriNetRelations;

import pcollections.PVector;

public class VisualPetriNetRelations {
	
	public PVector<Relation> getSchema() {
		throw new NotImplementedException();
	}
	
	public VisualPetriNetRelations(ObjectDeclaration visualTransition,
			ObjectDeclaration visualPlace, ObjectDeclaration visualArc,
			ObjectDeclaration visualTransformable) {
		// TODO Auto-generated constructor stub
	}

	public static VisualPetriNetRelations create(PetriNetRelations mathPetriNet) {
		ObjectDeclaration visualTransformable = new ObjectDeclaration.Instance("visualTransformable");
		
		ObjectDeclaration visualPlace = new ObjectDeclaration.Instance("visualPlace");
		ObjectDeclaration visualTransition = new ObjectDeclaration.Instance("visualTransition");
		ObjectDeclaration visualArc = new ObjectDeclaration.Instance("visualArc");
		
		Map<String, Field> visualPlaceFields = new HashMap<String, Field>();
		visualPlaceFields.put("mathNode", new RelationField.Instance(mathPetriNet.place.getObjectDeclaration(), DeletionPolicy.CASCADE_DELETE));
		visualPlaceFields.put("visualNode", new RelationField.Instance(visualTransformable, DeletionPolicy.CASCADE_DELETE));
		
		return new VisualPetriNetRelations(visualTransition, visualPlace, visualArc, visualTransformable);
	}
}