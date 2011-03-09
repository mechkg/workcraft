package org.workcraft.util;

import java.awt.Graphics2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.visual.GraphicalContent;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;

public class Graphics {
	public static Graphics2D cloneGraphics(Graphics2D g) {
		return (Graphics2D)g.create();
	}
	
	
	public static Function<GraphicalContent, GraphicalContent> statePreserver = new Function<GraphicalContent, GraphicalContent>(){
		@Override
		public GraphicalContent apply(final GraphicalContent content) {
			return new GraphicalContent(){
				@Override
				public void draw(Graphics2D graphics) {
					final Graphics2D g = cloneGraphics(graphics);
					try {
						content.draw(g);
					}
					finally {
						g.dispose();
					}
				}
			};
		}
	};
	
	public static Expression<? extends GraphicalContent> statePreserving(final Expression<? extends GraphicalContent> content) {
		return bindFunc(content, statePreserver);
	}
	
	public static Expression<? extends GraphicalContent> compose(final Expression<? extends GraphicalContent> bottom, final Expression<? extends GraphicalContent> top) {
		return new ExpressionBase<GraphicalContent>(){
			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent(){
					@Override
					public void draw(Graphics2D graphics) {
						Graphics2D clonedGraphics = cloneGraphics(graphics);
						try {
							context.resolve(bottom).draw(clonedGraphics);
							context.resolve(top).draw(graphics);
						}
						finally {
							clonedGraphics.dispose();
						}
					}
				};
			}
		};
	}
}
