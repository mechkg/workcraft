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

import org.workcraft.dependencymanager.advanced.core.GlobalCache;

public class MovableHelper {
	public static void translate(MovableNew m, double dx, double dy)
	{
		applyTransform(m, AffineTransform.getTranslateInstance(dx, dy));
	}

	public static void applyTransform(MovableNew m, AffineTransform transform)
	{
		AffineTransform old = GlobalCache.eval(m.transform());
		AffineTransform nw = new AffineTransform(old);
		nw.preConcatenate(transform);
		m.transform().setValue(nw);
	}

	public static void resetTransform(MovableNew m) {
		m.transform().setValue(new AffineTransform());
	}
}
