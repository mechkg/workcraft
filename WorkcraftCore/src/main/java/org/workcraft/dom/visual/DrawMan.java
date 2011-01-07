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
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;


interface GraphicalContentWithPreDecoration {
	void draw(Graphics2D graphics, Decorator decorator, Decoration currentDecoration);
}

public class DrawMan
{
	private final VisualModel model;

	public DrawMan(VisualModel model) {
		this.model = model;
	}
	
	private ExpressionBase<GraphicalContentWithPreDecoration> transformedAndDecorated(final MovableNew node)
	{
		return new ExpressionBase<GraphicalContentWithPreDecoration>() {
			@Override
			public GraphicalContentWithPreDecoration evaluate(EvaluationContext resolver) {
				final AffineTransform transform = resolver.resolve(node.transform());
				final GraphicalContentWithPreDecoration decorated = resolver.resolve(decorated(node));
				
				return new GraphicalContentWithPreDecoration() {
					@Override
					public void draw(Graphics2D graphics, Decorator decorator, Decoration decoration) {
						graphics.transform(transform);
						decorated.draw(graphics, decorator, decoration);
					}
				};
			}
		};
	}
	
	public ExpressionBase<HierarchicalGraphicalContent> graphicalContent(final Node node) {
		return new ExpressionBase<HierarchicalGraphicalContent>() {

			@Override
			public HierarchicalGraphicalContent evaluate(EvaluationContext resolver) {
				final GraphicalContentWithPreDecoration content = resolver.resolve(graphicalContextWithDefaultDecoration(node));
				return new HierarchicalGraphicalContent() {

					@Override
					public void draw(Graphics2D graphics, Decorator decorator) {
						content.draw(graphics, decorator, Decoration.Empty.INSTANCE);
					}
				};
			}
			
		};
	}
	
	public ExpressionBase<GraphicalContentWithPreDecoration> graphicalContextWithDefaultDecoration(final Node node) {
		return new ExpressionBase<GraphicalContentWithPreDecoration>() {
			@Override
			public GraphicalContentWithPreDecoration evaluate(EvaluationContext resolver) {
				
				if (node instanceof Hidable && resolver.resolve(((Hidable)node).hidden()))
					return IdleGraphicalContent.INSTANCE;
				
				final GraphicalContentWithPreDecoration toDraw;
				if (node instanceof MovableNew)
					toDraw = resolver.resolve(transformedAndDecorated((MovableNew)node));
				else
					toDraw = resolver.resolve(decorated(node));
				
				return new GraphicalContentWithPreDecoration() {
					@Override
					public void draw(Graphics2D graphics, Decorator decorator, Decoration currentDecoration) {
						Decoration decoration = decorator.getDecoration(node);
						if (decoration == null) decoration = currentDecoration;
						AffineTransform oldTransform = graphics.getTransform();
						toDraw.draw(graphics, decorator, decoration);
						graphics.setTransform(oldTransform);
					}
				};
			}
		};
	}
	
	private ExpressionBase<GraphicalContentWithPreDecoration> decorated(final Node node)
	{
		return new ExpressionBase<GraphicalContentWithPreDecoration>() {

				@Override
				public GraphicalContentWithPreDecoration evaluate(final EvaluationContext resolver) {
					
					Collection<? extends Node> children = resolver.resolve(node.children());
					final List<GraphicalContentWithPreDecoration> childrenGraphics = new ArrayList<GraphicalContentWithPreDecoration>();
					for(Node n : children)
						childrenGraphics.add(resolver.resolve(graphicalContextWithDefaultDecoration(n)));
					
					return new GraphicalContentWithPreDecoration() {
						@Override
						public void draw(final Graphics2D graphics, Decorator decorator, final Decoration decoration) {
							AffineTransform oldTransform = graphics.getTransform();
							DrawRequest drawRequest = new DrawRequest(){
								@Override
								public Decoration getDecoration() {
									return decoration;
								}
								@Override
								public Graphics2D getGraphics() {
									return graphics;
								}
								@Override
								public VisualModel getModel() {
									return model;
								}
							};
							if (node instanceof DrawableNew)
								resolver.resolve(((DrawableNew)node).graphicalContent()).draw(drawRequest);
							if (node instanceof Drawable) {
								(((Drawable)node)).draw(drawRequest);
							}
							graphics.setTransform(oldTransform);
							
							for (GraphicalContentWithPreDecoration child : childrenGraphics)
								child.draw(graphics, decorator, decoration);
						}
					};
				}
		};
	}
}