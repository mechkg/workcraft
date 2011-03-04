package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawRequest;

public class BezierControlPointGui {

	@Override
	public Expression<? extends ColorisableGraphicalContent> graphicalContent() {
		final Expression<? extends ColorisableGraphicalContent> superContentExpr = super.graphicalContent();
		return new ExpressionBase<ColorisableGraphicalContent>() {
			@Override
			protected ColorisableGraphicalContent evaluate(EvaluationContext context) {
				
				final ColorisableGraphicalContent superContent = context.resolve(superContentExpr);
				final Point2D orig = context.resolve(parentToLocalTransform()).transform(context.resolve(origin), null);

				return new ColorisableGraphicalContent() {

					@Override
					public void draw(DrawRequest r) {
						Graphics2D g = r.getGraphics();
						
						g.setColor(Color.LIGHT_GRAY);
						g.setStroke(new BasicStroke(0.02f));
						
						Line2D l = new Line2D.Double(0, 0, orig.getX(), orig.getY());
						g.draw(l);
						
						superContent.draw(r);
					}
					
				};
			}
		};
	}
}
