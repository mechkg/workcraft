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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.util.Function;

public class BoundingBoxHelper {

	public static final Function<Collection<? extends Rectangle2D.Double.Double>, Rectangle2D.Double.Double> mergeBoundingBoxes = 
			new Function<Collection<? extends Rectangle2D.Double.Double>, Rectangle2D.Double.Double>() {
		@Override
		public Rectangle2D.Double.Double apply(Collection<? extends Rectangle2D.Double.Double> argument) {
			Rectangle2D.Double.Double bb = null;
			for(Rectangle2D.Double.Double rect : argument)
				bb = union(bb, rect);
			return bb;
		}
	};

	public static Rectangle2D.Double.Double union(Rectangle2D.Double.Double rect1, Rectangle2D.Double.Double rect2)
	{
		if (rect1 == null) return rect2;
		if (rect2 == null) return rect1;
		
		Rectangle2D.Double.Double result = new Rectangle2D.Double.Double();

		result.setRect(rect1);
		result.add(rect2);
		
		return result;
	}

	public static Rectangle2D.Double mergeBoundingBoxes(Collection<? extends Touchable> nodes) {
		Rectangle2D.Double.Double bb = null;
		for(Touchable node : nodes)
			bb = union(bb, node.getBoundingBox());
		return bb;
	}
	
	public static Rectangle2D.Double expand(Rectangle2D.Double rect, double x, double y)
	{
		if(rect == null)
			return null;
		
		Rectangle2D.Double res = new Rectangle2D.Double.Double();
		res.setRect(rect);
		
		x /= 2.0f;
		y /= 2.0f;
		
		res.add(rect.getMinX() - x, rect.getMinY() - y);
		res.add(rect.getMaxX() + x, rect.getMaxY() + y);

		return res;
	}

	public static Rectangle2D.Double transform(Rectangle2D.Double rect, AffineTransform transform)
	{
		if(rect == null)
    		return null;
		
		Point2D.Double p0 = new Point2D.Double.Double(rect.getMinX(), rect.getMinY()); 
		Point2D.Double p1 = new Point2D.Double.Double(rect.getMaxX(), rect.getMaxY());
		
		transform.transform(p0, p0);
		transform.transform(p1, p1);

		Rectangle2D.Double.Double result = new Rectangle2D.Double.Double(p0.getX(), p0.getY(), 0, 0);
		result.add(p1);
		
		return result;
	}
}
