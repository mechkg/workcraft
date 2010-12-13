package org.workcraft.relational.petrinet.typeunsafe;

import java.awt.geom.AffineTransform;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.relational.engine.DatabaseEngine;
import org.workcraft.relational.engine.Id;
import org.workcraft.relational.petrinet.model.VeryAbstractVisualGroup;

import static org.workcraft.relational.petrinet.model.DatabaseUtils.*;

public class VisualGroup {
	public static VeryAbstractVisualGroup wrap(DatabaseEngine engine, Id id) {
		
		ModifiableExpression<Id> visualTransformableId = fieldValue(engine, "visualGroup", "visualTransformable", Id.class, id);
		ModifiableExpression<Id> visualNodeId = fieldValue(engine, "visualGroup", "visualNode", Id.class, id);
		ModifiableExpression<AffineTransform> transformableTransform = fieldValue(engine, "visualTransformable", "transform", AffineTransform.class, id);
		return new VeryAbstractVisualGroup(
				;
	}
}
