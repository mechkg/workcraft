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

package org.workcraft.testing.plugins.petri;

import java.awt.geom.Point2D;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.DefaultStorageManager;

public class VisualPlaceTests {
	@Test
	public void TestHitTest() {
		Place p = new Place(new DefaultStorageManager());
		VisualPlace vp = new VisualPlace(p, new DefaultStorageManager());
		
		TouchableProvider<Node> tp = TouchableProvider.DEFAULT;
		
		// since 24.02.2011 the Movable is applied on top of shape, so shape does not change anymore
		// TouchableProvider.REFLECTIVE_WITH_TRANSLATIONS manages it instead.
		Assert.assertTrue(GlobalCache.eval(vp.shape()).hitTest(new Point2D.Double(0,0)));
		Assert.assertFalse(GlobalCache.eval(vp.shape()).hitTest(new Point2D.Double(5,5)));
		
		Expression<? extends Touchable> transformedShape = tp.apply(vp);
		
		Assert.assertTrue(GlobalCache.eval(transformedShape).hitTest(new Point2D.Double(0,0)));
		Assert.assertFalse(GlobalCache.eval(transformedShape).hitTest(new Point2D.Double(5,5)));
		
		vp.x().setValue(5.0);
		vp.y().setValue(5.0);
		
		Assert.assertFalse(GlobalCache.eval(vp.shape()).hitTest(new Point2D.Double(5,5)));
		Assert.assertTrue(GlobalCache.eval(vp.shape()).hitTest(new Point2D.Double(0,0)));

		Assert.assertFalse(GlobalCache.eval(transformedShape).hitTest(new Point2D.Double(0,0)));
		Assert.assertTrue(GlobalCache.eval(transformedShape).hitTest(new Point2D.Double(5,5)));
	}
}
