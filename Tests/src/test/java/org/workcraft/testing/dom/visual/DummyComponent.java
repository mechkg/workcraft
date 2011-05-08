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

package org.workcraft.testing.dom.visual;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.Container;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.ReflectiveTouchable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.stg.DefaultStorageManager;

class SquareNode extends VisualComponent implements ReflectiveTouchable
{
	Rectangle2D.Double rectOuter;
	Rectangle2D.Double rectInner;
	int resultToReturn;
	public SquareNode(Container parent, Rectangle2D.Double rectOuter, Rectangle2D.Double rectInner) {
		super(null, new DefaultStorageManager());
		this.rectOuter = rectOuter;
		this.rectInner = rectInner;
	}

	public SquareNode(Container parent, Rectangle2D.Double rect) {
		this(parent, rect, rect);
	}

	@Override
	public String toString() {
		return rectInner.toString();
	}

	@Override
	public Expression<? extends Touchable> shape() {
		return Expressions.constant(new Touchable() {
			@Override
			public Rectangle2D getBoundingBox() {
				return rectOuter;
			};
			@Override
			public Point2D getCenter() {
				return new Point2D.Double(rectOuter.getCenterX(), rectOuter.getCenterY());
			}
			@Override
			public boolean hitTest(Point2D point) {
				return rectInner.contains(point);
			}
		});
	}
	@Override
	public Collection<MathNode> getMathReferences() {
		return Arrays.asList(new MathNode[]{});
	}
}

