package org.workcraft.util;

import java.awt.Graphics2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.visual.GraphicalContent;

public class Graphics {
	public static Graphics2D cloneGraphics(Graphics2D g) {
		return (Graphics2D)g.create();
	}
	
	public static Expression<? extends GraphicalContent> statePreserving(final Expression<? extends GraphicalContent> content) {
		return new ExpressionBase<GraphicalContent>(){

			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent(){

					@Override
					public void draw(Graphics2D graphics) {
						final Graphics2D g = cloneGraphics(graphics);
						try {
							context.resolve(content).draw(g);
						}
						finally {
							g.dispose();
						}
					}
					
				};
			}
			
		};
	}
	
	public static Expression<? extends GraphicalContent> compose(final Expression<? extends GraphicalContent> bottom, final Expression<? extends GraphicalContent> top) {
		return new ExpressionBase<GraphicalContent>(){

			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent(){

					@Override
					public void draw(Graphics2D graphics) {
						context.resolve(bottom).draw(cloneGraphics(graphics));
						context.resolve(top).draw(graphics);
					}
					
				};
			}
			
		};
	}
}
