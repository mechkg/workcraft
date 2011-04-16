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

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.Node;
import org.workcraft.gui.graph.tools.NodePainter;
import org.workcraft.util.Function;
import org.workcraft.util.Function2;
import org.workcraft.util.Graphics;

public class DrawMan
{
	public static Expression<? extends GraphicalContent> graphicalContent(final Node node, final NodePainter gcProvider) {
		Expression<? extends GraphicalContent> withoutTransform = withoutTransform(node, gcProvider);
		if (node instanceof MovableNew)
			return applyTransform((MovableNew)node, withoutTransform);
		else
			return withoutTransform;
	}
	
	private static Expression<GraphicalContent> applyTransform(final MovableNew node, final Expression<? extends GraphicalContent> gc)
	{
		return fmap(new Function2<AffineTransform, GraphicalContent, GraphicalContent>(){
			@Override
			public GraphicalContent apply(final AffineTransform transform, final GraphicalContent content) {
				return new GraphicalContent() {
					@Override
					public void draw(Graphics2D graphics) {
						graphics.transform(transform);
						content.draw(graphics);
					}
				};
			}
		}, node.transform(), gc);
	}
	
	public static <N> Expression<GraphicalContent> drawCollection(Iterable<? extends N> collection, Function<? super N, ? extends Expression<? extends GraphicalContent>> painter) {
		Iterable<? extends N> children = collection;
		final List<Expression<? extends GraphicalContent>> childrenGraphics = new ArrayList<Expression<? extends GraphicalContent>>();
		for(N n : children)
			childrenGraphics.add(painter.apply(n));
		
		Expression<GraphicalContent> result = constant(GraphicalContent.EMPTY);
		for(Expression<? extends GraphicalContent> child : childrenGraphics)
			result = Graphics.compose(result, child);
		
		return result;
	}

	private static Expression<? extends GraphicalContent> withoutTransform(final Node node, final NodePainter gcProvider)
	{
		final Expression<? extends GraphicalContent> graphicalContent = gcProvider.getGraphicalContent(node);
		final Expression<? extends Collection<? extends Node>> nodes = node.children();
		Expression<? extends Expression<? extends GraphicalContent>> childrenGc = new ExpressionBase<Expression<? extends GraphicalContent>>(){
			@Override
			protected Expression<? extends GraphicalContent> evaluate(EvaluationContext context) {
				return drawCollection(context.resolve(nodes), new Function<Node, Expression<? extends GraphicalContent>>(){
					@Override
					public Expression<? extends GraphicalContent> apply(Node n) {
						return graphicalContent(n, gcProvider);
					}
				});
			}
		};
		
		return Graphics.compose(graphicalContent, Expressions.join(childrenGc));
	}
}
