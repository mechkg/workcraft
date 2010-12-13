package org.workcraft.relational.petrinet.model;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.DrawableNew;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.relational.petrinet.generated.VisualPlaceId;

public class VisualPlaceNode implements Node, DrawableNew {

	public VisualPlaceNode(Expression<VisualPetriNetData> data, VisualPlaceId place) {
		this.data = data;
		this.place = place;
	}
	
	Expression<VisualPetriNetData> data;
	VisualPlaceId place;
	
	@Override
	public Expression<? extends Touchable> shape() {
		return null;
	}

	@Override
	public ModifiableExpression<Node> parent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<? extends Collection<? extends Node>> children() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<? extends GraphicalContent> graphicalContent() {
		return new ExpressionBase<GraphicalContent>(){
			@Override
			protected GraphicalContent evaluate(EvaluationContext context) {
				return null;
			}
		};
	}

}
