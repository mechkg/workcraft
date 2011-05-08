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

package org.workcraft.plugins.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawableNew;
import org.workcraft.dom.visual.ReflectiveTouchable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class VisualVertex extends VisualComponent implements DrawableNew, ReflectiveTouchable {
	private static double size = 1;
	private static float strokeWidth = 0.1f;

	public VisualVertex(Vertex vertex, StorageManager storage) {
		super(vertex, storage);
	}

	@Override
	public Expression<? extends ColorisableGraphicalContent> graphicalContent() {
		return Expressions.constant(new ColorisableGraphicalContent(){

			@Override
			public void draw(DrawRequest r) {

				Shape shape = new Ellipse2D.Double(
						-size/2+strokeWidth/2,
						-size/2+strokeWidth/2,
						size-strokeWidth,
						size-strokeWidth);
				
				Graphics2D g = r.getGraphics();

				g.setStroke(new BasicStroke(strokeWidth));

				g.setColor(Color.WHITE);
				g.fill(shape);
				g.setColor(Color.BLACK);
				g.draw(shape);
			}
		});
	}

	@Override
	public Expression<? extends Touchable> shape() {
		return Expressions.constant(new Touchable(){

			@Override
			public boolean hitTest(Point2D point) {
				double size = CommonVisualSettings.getSize();
				
				return point.distanceSq(0, 0) < size*size/4;
			}

			@Override
			public Rectangle2D getBoundingBox() {
				double size = CommonVisualSettings.getSize();
				return new Rectangle2D.Double(-size/2, -size/2, size, size);	
			}

			@Override
			public Point2D getCenter() {
				Rectangle2D bb = getBoundingBox();
				return new Point2D.Double(bb.getCenterX(), bb.getCenterY());
			}
			
		});
	}

}
