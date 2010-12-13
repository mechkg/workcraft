package org.workcraft.relational.petrinet.model;

import java.awt.geom.AffineTransform;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.relational.engine.DatabaseEngine;
import org.workcraft.relational.engine.Id;

public class TransformableNode {
	public TransformableNode(DatabaseEngine dbEngine, Id visualTransformableId) {
		this.dbEngine = dbEngine;
		this.visualTransformableId = visualTransformableId;
	}
	private final DatabaseEngine dbEngine;
	private final Id visualTransformableId;
	
	ModifiableExpression<AffineTransform> transform() {
		return dbEngine.
	};
}
