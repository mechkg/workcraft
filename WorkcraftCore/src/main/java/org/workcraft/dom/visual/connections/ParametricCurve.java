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

package org.workcraft.dom.visual.connections;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface ParametricCurve {
	public Point2D getPointOnCurve (double t);
	public double getNearestPointT (Point2D pt);
	public Rectangle2D getBoundingBox();
	
	// TODO: subdivide the parametric curve automatically, removing the need for manual Shape creation
	public Shape getShape(double tStart, double tEnd);	

	public Point2D getDerivativeAt(double t);
	public Point2D getSecondDerivativeAt(double t);
}
