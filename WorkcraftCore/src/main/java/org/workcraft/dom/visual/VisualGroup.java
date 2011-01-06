/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.gui.Coloriser;
import org.workcraft.util.Hierarchy;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class VisualGroup extends VisualTransformableNode implements DrawableNew, Container {
	public static final int HIT_COMPONENT = 1;
	public static final int HIT_CONNECTION = 2;
	public static final int HIT_GROUP = 3;

	DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

	@Override
	public ExpressionBase<GraphicalContent> graphicalContent() {
		return new ExpressionBase<GraphicalContent>() {

			@Override
			public GraphicalContent evaluate(EvaluationContext resolver) {
				final Rectangle2D bb = resolver.resolve(localSpaceTouchable()).getBoundingBox();
				final Node parent = resolver.resolve(parent());
				
				return new GraphicalContent() {
					
					@Override
					public void draw(DrawRequest r) {
						if (bb != null && parent != null) {
							bb.setRect(bb.getX() - 0.1, bb.getY() - 0.1, bb.getWidth() + 0.2, bb.getHeight() + 0.2);
							r.getGraphics().setColor(Coloriser.colorise(Color.GRAY, r.getDecoration().getColorisation()));
							r.getGraphics().setStroke(new BasicStroke(0.02f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[]{0.2f, 0.2f}, 0.0f));
							r.getGraphics().draw(bb);
						}
					}
				};
			}
		};
	}

	@Override
	public Expression<Touchable> localSpaceTouchable() {
		return new ExpressionBase<Touchable>() {
			@Override
			protected Touchable evaluate(final EvaluationContext context) {
				return new Touchable() {
					
					@Override
					public boolean hitTest(Point2D point) {
						return false;
					}
					
					@Override
					public Point2D getCenter() {
						return new Point2D.Double(0, 0);
					}
					
					@Override
					public Rectangle2D getBoundingBox() {
						Rectangle2D result = null;
						for(Node n : context.resolve(children())) {
							Touchable shape = context.resolve(n.shape());
							if(shape!=null) {
								result = BoundingBoxHelper.union(result, shape.getBoundingBox());
							}
						}
						return result;
					}
				};
			}
		};
	}

	public List<Node> unGroup() {
		ArrayList<Node> nodesToReparent = new ArrayList<Node>(eval(groupImpl.children()));
		
		Container newParent = Hierarchy.getNearestAncestor(eval(parent()), Container.class);

		groupImpl.reparent(nodesToReparent, newParent);

		for (Node node : nodesToReparent)
			TransformHelper.applyTransform(node, localToParentTransform.getValue());
		
		return nodesToReparent;
	}
	
	@Override
	public void add(Node node) {
		groupImpl.add(node);
	}

	@Override
	public ModifiableExpression<Node> parent() {
		return groupImpl.parent();
	}

	@Override
	public void remove(Node node) {
		groupImpl.remove(node);
	}
	
	@Override
	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
	}

	@Override
	public void remove(Collection<Node> nodes) {
		groupImpl.remove(nodes);
	}

	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
	}


	@Override
	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}

	@Override
	public ExpressionBase<? extends Collection<Node>> children() {
		return groupImpl.children();
	}
}