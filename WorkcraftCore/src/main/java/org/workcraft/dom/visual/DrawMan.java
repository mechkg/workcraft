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
import org.workcraft.util.Graphics;

public class DrawMan
{
	public static ExpressionBase<GraphicalContent> graphicalContent(final Node node, final NodePainter gcProvider) {
		return new ExpressionBase<GraphicalContent>() {
			@Override
			public GraphicalContent evaluate(EvaluationContext resolver) {
				if (node instanceof Hidable && resolver.resolve(((Hidable)node).hidden()))
					return GraphicalContent.EMPTY;
				else if (node instanceof MovableNew)
					return resolver.resolve(transformedAndDecorated((MovableNew)node, gcProvider));
				else
					return resolver.resolve(decorated(node, gcProvider));
			}
		}; 
	}
	
	private static ExpressionBase<GraphicalContent> transformedAndDecorated(final MovableNew node, final NodePainter gcProvider)
	{
		return new ExpressionBase<GraphicalContent>() {
			@Override
			public GraphicalContent evaluate(EvaluationContext resolver) {
				final AffineTransform transform = resolver.resolve(node.transform());
				final GraphicalContent decorated = resolver.resolve(decorated(node, gcProvider));
				
				return new GraphicalContent() {
					@Override
					public void draw(Graphics2D graphics) {
						graphics.transform(transform);
						decorated.draw(graphics);
					}
				};
			}
		};
	}

	private static Expression<? extends GraphicalContent> decorated(final Node node, final NodePainter gcProvider)
	{
		final Expression<? extends GraphicalContent> graphicalContent = gcProvider.getGraphicalContent(node);
		Expression<? extends Expression<? extends GraphicalContent>> childrenGc = new ExpressionBase<Expression<? extends GraphicalContent>>(){

			@Override
			protected Expression<? extends GraphicalContent> evaluate(EvaluationContext context) {
				Collection<? extends Node> children = context.resolve(node.children());
				final List<ExpressionBase<GraphicalContent>> childrenGraphics = new ArrayList<ExpressionBase<GraphicalContent>>();
				for(Node n : children)
					childrenGraphics.add(graphicalContent(n, gcProvider));
				
				Expression<? extends GraphicalContent> result = graphicalContent;
				for(ExpressionBase<GraphicalContent> child : childrenGraphics)
					result = Graphics.compose(result, child);
				
				return result;
			}
		};
		
		return Expressions.join(childrenGc);
	}
}
