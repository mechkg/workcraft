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

/**
 * 
 */
package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.GraphicalContent;

public class BezierControlPoint extends ControlPoint {
	private Expression<Point2D> origin;
	
	public BezierControlPoint(Expression<Point2D> origin) {
		this.origin = origin;
	}

	@Override
	public Expression<? extends GraphicalContent> graphicalContent() {
		final Expression<? extends GraphicalContent> superContentExpr = super.graphicalContent();
		return new ExpressionBase<GraphicalContent>() {
			@Override
			protected GraphicalContent evaluate(EvaluationContext context) {
				
				final GraphicalContent superContent = context.resolve(superContentExpr);
				final Point2D orig = context.resolve(parentToLocalTransform()).transform(context.resolve(origin), null);

				return new GraphicalContent() {

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
	
	@Override
	public Expression<Boolean> hidden() {
		return new ExpressionBase<Boolean>(){
			@Override
			protected Boolean evaluate(EvaluationContext context) {
				return context.resolve(((Bezier)context.resolve(parent())).controlsHidden());
			}
		};
	}
}
