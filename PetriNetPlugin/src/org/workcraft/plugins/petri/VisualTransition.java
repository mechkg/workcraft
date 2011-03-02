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

package org.workcraft.plugins.petri;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawableNew;
import org.workcraft.dom.visual.ReflectiveTouchable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class VisualTransition extends VisualComponent implements DrawableNew, ReflectiveTouchable {

	public VisualTransition(Transition transition, StorageManager storage) {
		super(transition, storage);
	}
	
	public Transition getReferencedTransition() {
		return (Transition)getReferencedComponent();
	}
	
	@Override
	public Expression<? extends ColorisableGraphicalContent> graphicalContent() {
		return new ExpressionBase<ColorisableGraphicalContent>() {
			@Override
			protected ColorisableGraphicalContent evaluate(final EvaluationContext context) {
				return new ColorisableGraphicalContent() {
					@Override
					public void draw(DrawRequest r) {
						context.resolve(labelGraphics()).draw(r);
						
						Graphics2D g = r.getGraphics();
						
						double size = context.resolve(CommonVisualSettings.size);
						double strokeWidth = context.resolve(CommonVisualSettings.strokeWidth);
						
						Shape shape = new Rectangle2D.Double(
								-size / 2 + strokeWidth / 2,
								-size / 2 + strokeWidth / 2,
								size - strokeWidth,
								size - strokeWidth);
						g.setColor(Coloriser.colorise(Coloriser.colorise(context.resolve(fillColor()), r.getColorisation().getBackground()), r.getColorisation().getColorisation()));
						g.fill(shape);
						g.setColor(Coloriser.colorise(Coloriser.colorise(context.resolve(foregroundColor()), r.getColorisation().getBackground()), r.getColorisation().getColorisation()));
						g.setStroke(new BasicStroke((float)(double)context.resolve(CommonVisualSettings.strokeWidth)));
						g.draw(shape);

					}
				};
			}
		};
	}
	
	@Override
	public Expression<Touchable> shape() {
		return new ExpressionBase<Touchable>() {

			@Override
			protected Touchable evaluate(final EvaluationContext context) {
				return new Touchable() {

					@Override
					public boolean hitTest(Point2D point) {
						return getBoundingBox().contains(point);
					}

					@Override
					public Rectangle2D getBoundingBox() {
						double size = context.resolve(CommonVisualSettings.size); 
						return new Rectangle2D.Double(-size/2, -size/2, size, size);
					}

					@Override
					public Point2D getCenter() {
						return new Point2D.Double(0, 0);
					}
				};
			}
		};
	}
}
