package org.workcraft.relational.petrinet.typeunsafe;

import static org.workcraft.dependencymanager.advanced.core.Expressions.constant;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;
import static org.workcraft.relational.petrinet.model.DatabaseUtils.fieldValue;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Node;
import org.workcraft.relational.engine.DatabaseEngine;
import org.workcraft.relational.engine.Id;
import org.workcraft.relational.petrinet.model.DatabaseUtils;
import org.workcraft.relational.petrinet.model.VisualGroupNode;
import org.workcraft.relational.petrinet.model.VisualPlaceNode;
import org.workcraft.relational.petrinet.model.VisualTransitionNode;
import org.workcraft.util.ExpressionUtil;

import pcollections.PVector;
import pcollections.TreePVector;

public class TypeUnsafePetriNetWrapper {
	public static VisualGroupNode wrapVisualGroup(DatabaseEngine engine, Id id) {
		
		ModifiableExpression<Id> visualNodeId = fieldValue(engine, "visualGroup", "visualNode", Id.class, id);
		ModifiableExpression<AffineTransform> transformableTransform = visualNodeTransform(engine, visualNodeId);
		Expression<PVector<Node>> children = getChildren(engine, constant(id));
		Expression<Node> parent = getVisualNodeParent(engine, visualNodeId); 
		return new VisualGroupNode(transformableTransform, children, ExpressionUtil.modificationNotSupported(parent), id);
	}

	private static Expression<Node> getVisualNodeParent(DatabaseEngine engine, Expression<Id> visualNodeId) {
		return wrapNodeExpression(engine, fieldValue(engine, "visualNode", "parent", Id.class, visualNodeId));
	}

	private static ModifiableExpression<AffineTransform> visualNodeTransform(DatabaseEngine engine, Expression<Id> visualNodeId) {
		return fieldValue(engine, "visualNode", "transform", AffineTransform.class, visualNodeId);
	}

	private static Expression<Node> wrapNodeExpression(final DatabaseEngine engine, final Expression<Id> id) {
		return new ExpressionBase<Node>(){

			@Override
			protected Node evaluate(EvaluationContext context) {
				return wrapNode(engine, context.resolve(id));
			}
			
		};
	}

	private static Expression<PVector<Node>> getChildren(final DatabaseEngine engine, Expression<Id> visualGroupId) {

		final Expression<PVector<Id>> childNodeIds = DatabaseUtils.children(engine, "visualGroup", "visualNode", "parent", visualGroupId);
		
		return new ExpressionBase<PVector<Node>>(){

			@Override
			protected PVector<Node> evaluate(EvaluationContext context) {
				PVector<Node> result = TreePVector.<Node>empty();
				for(Id id : context.resolve(childNodeIds)) {
					result = result.plus(wrapNode(engine, id));
				}
				return result;
			}
			
		};
	}

	static Id singleOrNull(Collection<Id> collection) {
		if(collection.size() == 0)
			return null;
		if(collection.size() == 1)
			return collection.iterator().next();
		throw new RuntimeException("too many objects");
	}
	
	private static Node wrapNode(DatabaseEngine engine, Id id) {
		Id asVisualGroup = singleOrNull(eval(DatabaseUtils.children(engine, "visualNode", "visualGroup", "visualNode", constant(id))));
		Id asVisualPlace = singleOrNull(eval(DatabaseUtils.children(engine, "visualNode", "visualPlace", "visualNode", constant(id))));
		Id asVisualTransition = singleOrNull(eval(DatabaseUtils.children(engine, "visualNode", "visualTransition", "visualNode", constant(id))));
		return 
			asVisualGroup != null ? wrapVisualGroup(engine, asVisualGroup) :
			asVisualPlace != null ? wrapVisualPlace(engine, asVisualPlace) :
			asVisualTransition != null ? wrapVisualTransition(engine, asVisualTransition) : null;
	}

	private static Node wrapVisualPlace(DatabaseEngine engine, Id id) {
		Expression<Id> asVisualNode = fieldValue(engine, "visualPlace", "visualNode", Id.class, id);
		Expression<Id> asMathPlace = fieldValue(engine, "visualPlace", "mathNode", Id.class, id);
		Expression<Integer> tokenCount = fieldValue(engine, "place", "initialMarking", Integer.class, asMathPlace);
		ModifiableExpression<Color> tokenColor = fieldValue(engine, "visualPlace", "tokenColor", Color.class, id);
		Expression<Node> parent = getVisualNodeParent(engine, asVisualNode);
		return new VisualPlaceNode(visualNodeTransform(engine, asVisualNode), tokenCount, parent, tokenColor);
	}

	private static Node wrapVisualTransition(DatabaseEngine engine, Id id) {
		Expression<Id> asVisualNode = fieldValue(engine, "visualTransition", "visualNode", Id.class, id);
//		Expression<Id> asMathPlace = fieldValue(engine, "visualPlace", "mathNode", Id.class, id);
		Expression<Node> parent = getVisualNodeParent(engine, asVisualNode);
		return new VisualTransitionNode(visualNodeTransform(engine, asVisualNode), parent);
	}
}
