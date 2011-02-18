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
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.Node;
import org.workcraft.gui.graph.tools.NodeGraphicalContentProvider;
import org.workcraft.util.GUI;

public class DrawMan
{
	public static ExpressionBase<GraphicalContent> graphicalContent(final Node node, final NodeGraphicalContentProvider gcProvider) {
		return new ExpressionBase<GraphicalContent>() {
			@Override
			public GraphicalContent evaluate(EvaluationContext resolver) {
				if (node instanceof Hidable && resolver.resolve(((Hidable)node).hidden()))
					return GraphicalContent.empty;
				else if (node instanceof MovableNew)
					return resolver.resolve(transformedAndDecorated((MovableNew)node, gcProvider));
				else
					return resolver.resolve(decorated(node, gcProvider));
			}
		}; 
	}
	
	private static ExpressionBase<GraphicalContent> transformedAndDecorated(final MovableNew node, final NodeGraphicalContentProvider gcProvider)
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

	private static ExpressionBase<GraphicalContent> decorated(final Node node, final NodeGraphicalContentProvider gcProvider)
	{
		return new ExpressionBase<GraphicalContent>() {
				@Override
				public GraphicalContent evaluate(final EvaluationContext resolver) {
					
					Collection<? extends Node> children = resolver.resolve(node.children());
					final List<GraphicalContent> childrenGraphics = new ArrayList<GraphicalContent>();
					for(Node n : children)
						childrenGraphics.add(resolver.resolve(graphicalContent(n, gcProvider)));
					
					final GraphicalContent nodeGraphicalContent = gcProvider.getGraphicalContent(node);
					
					return new GraphicalContent() {
						@Override
						public void draw(final Graphics2D graphics) {
							nodeGraphicalContent.draw(GUI.cloneGraphics(graphics));
							
							for (GraphicalContent child : childrenGraphics)
								child.draw(GUI.cloneGraphics(graphics));
						}
					};
				}
		};
	}
}