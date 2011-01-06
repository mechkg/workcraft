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

package org.workcraft.plugins.circuit;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;

@DisplayName("Joint")
@Hotkey(KeyEvent.VK_J)
@SVGIcon("images/icons/svg/circuit-joint.svg")

public class VisualJoint extends VisualComponent {
	static double jointSize = 0.25;
	
	public VisualJoint(Joint joint) {
		super(joint);
		
	}

	@Override
	public Expression<? extends GraphicalContent> graphicalContent() {
		return new ExpressionBase<GraphicalContent>(){
			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent() {
					
					@Override
					public void draw(DrawRequest r) {
//							drawLabelInLocalSpace(g);
						Graphics2D g = r.getGraphics();
						
						Shape shape = new Ellipse2D.Double(
								-jointSize / 2,
								-jointSize / 2,
								jointSize,
								jointSize);
						
						g.setColor(Coloriser.colorise(context.resolve(foregroundColor()), r.getDecoration().getColorisation()));
						g.fill(shape);
					}
				};
			}
		};
	}
	
	
	@Override
	public Expression<? extends Touchable> localSpaceTouchable() {
		return Expressions.constant(new Touchable() {
			
			@Override
			public boolean hitTest(Point2D point) {
				return point.distanceSq(0, 0) < jointSize*jointSize/4;
			}
			
			@Override
			public Point2D getCenter() {
				return new Point2D.Double(0,0);
			}
			
			@Override
			public Rectangle2D getBoundingBox() {
				return new Rectangle2D.Double(-jointSize/2, -jointSize/2, jointSize, jointSize);
			}
		});
	}

}
