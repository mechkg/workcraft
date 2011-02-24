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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawableNew;
import org.workcraft.dom.visual.ReflectiveTouchable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.petri.Place;

public class VisualJoint extends VisualComponent implements DrawableNew, ReflectiveTouchable {
	static public double jointSize = 0.25;
	
	final static public Shape shape = new Ellipse2D.Double(
			-jointSize / 2,
			-jointSize / 2,
			jointSize,
			jointSize);
	
	private Place referencedOnePlace;
	public Place getReferencedOnePlace() {
		return referencedOnePlace;
	}

	public void setReferencedOnePlace(Place referencedOnePlace) {
		this.referencedOnePlace = referencedOnePlace;
	}

	public Place getReferencedZeroPlace() {
		return referencedZeroPlace;
	}

	public void setReferencedZeroPlace(Place referencedZeroPlace) {
		this.referencedZeroPlace = referencedZeroPlace;
	}

	private Place referencedZeroPlace;
	
	public VisualJoint(Joint joint, StorageManager storage) {
		super(joint, storage);
		
	}

	@Override
	public Expression<? extends ColorisableGraphicalContent> graphicalContent() {
		return new ExpressionBase<ColorisableGraphicalContent>(){
			@Override
			protected ColorisableGraphicalContent evaluate(final EvaluationContext context) {
				return new ColorisableGraphicalContent() {
					
					@Override
					public void draw(DrawRequest r) {
//							drawLabelInLocalSpace(g);
						Graphics2D g = r.getGraphics();
						
		
						
						g.setColor(Coloriser.colorise(context.resolve(foregroundColor()), r.getColorisation().getColorisation()));
						g.fill(shape);
					}
				};
			}
		};
	}
	
	@Override
	public Expression<? extends Touchable> shape() {
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
