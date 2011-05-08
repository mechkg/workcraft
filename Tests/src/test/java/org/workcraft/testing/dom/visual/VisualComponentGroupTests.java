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
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnectionProperties;
import org.workcraft.plugins.stg.DefaultStorageManager;

public class VisualComponentGroupTests {

	@Test
	public void TestHitComponent()
	{
		VisualGroup root = createGroup(null);
		VisualGroup node1 = createGroup(root);
		node1.x().setValue(5.0);
		
		SquareNode sq1 = new SquareNode(root, new Rectangle2D.Double(1, 1, 1, 1));
		SquareNode sq2 = new SquareNode(node1, new Rectangle2D.Double(2, 2, 1, 1));
		root.add(sq1);
		node1.add(sq2);
		
		Assert.assertEquals(sq1, HitMan.hitDeepestNodeOfType(TouchableProvider.DEFAULT, new Point2D.Double(1.5, 1.5), root, VisualComponent.class));
		Assert.assertEquals(sq2, HitMan.hitDeepestNodeOfType(TouchableProvider.DEFAULT, new Point2D.Double(7.5, 2.5), root, VisualComponent.class));
		Assert.assertEquals(null, HitMan.hitDeepestNodeOfType(TouchableProvider.DEFAULT, new Point2D.Double(2.5, 2.5), root, VisualComponent.class));
	}

	public void TestHitConnection()
	{
		VisualGroup root = createGroup(null);
		VisualGroup group = createGroup(root);
		group.x().setValue(5.0);
		
		SquareNode sqr1 = new SquareNode(root, new Rectangle2D.Double(1, 1, 1, 1));
		SquareNode sqr2 = new SquareNode(root, new Rectangle2D.Double(3, 3, 1, 1));
		root.add(sqr1);
		root.add(sqr2);
		VisualConnectionProperties connectionR = GlobalCache.eval(Tools.createConnection(null, sqr1, sqr2, root).properties());
		
		SquareNode sqg1 = new SquareNode(group, new Rectangle2D.Double(1, 1, 1, 1));
		SquareNode sqg2 = new SquareNode(group, new Rectangle2D.Double(3, 3, 1, 1));
		group.add(sqg1);
		group.add(sqg2);
		Tools.createConnection(null, sqg1, sqg2, group);
		
		Assert.assertEquals(connectionR, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(2.5, 1.5), root));
		Assert.assertEquals(group, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(7.5, 1.5), root));
	}

	@Test
	public void TestHitNode()
	{
		VisualGroup group = new VisualGroup(new DefaultStorageManager());
		
		Rectangle2D.Double r1 = new Rectangle2D.Double();
		Rectangle2D.Double r1_ = new Rectangle2D.Double();
		Rectangle2D.Double r2 = new Rectangle2D.Double();
		Rectangle2D.Double r2_ = new Rectangle2D.Double();
		Rectangle2D.Double r3 = new Rectangle2D.Double();
		Rectangle2D.Double r3_ = new Rectangle2D.Double();
		
		r1.setRect(0, 0, 2, 2);
		r1_.setRect(0.1, 0.1, 1.8, 1.8);
		r2.setRect(0.5, 0.5, 2, 2);
		r2_.setRect(0.6, 0.6, 1.8, 1.8);
		r3.setRect(1, 1, 2, 2);
		r3_.setRect(1.1, 1.1, 1.8, 1.8);
		
		VisualNode node1 = new SquareNode(group, r1, r1_);
		VisualNode node2 = new SquareNode(group, r2, r2_);
		VisualNode node3 = new SquareNode(group, r3, r3_);
		
		Assert.assertNull(GlobalCache.eval(VisualGroup.screenSpaceBounds(TouchableProvider.DEFAULT, group)).getBoundingBox());
		
		group.add(node1);
		group.add(node2);
		group.add(node3);
		Assert.assertNull(HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(-1, -1), group));
		Assert.assertNull(HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(10, 10), group));
		Assert.assertNull(HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(0.05, 0.05), group));
		Assert.assertEquals(node1, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(0.15, 0.5), group));
		Assert.assertEquals(node1, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(0.55, 0.55), group));
		Assert.assertEquals(node2, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(0.65, 0.65), group));
		Assert.assertEquals(node2, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(1.05, 1.05), group));
		Assert.assertEquals(node3, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(1.15, 1.15), group));
		Assert.assertEquals(node3, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(1.95, 1.95), group));
		Assert.assertEquals(node3, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(2.35, 1.35), group));
		Assert.assertEquals(node3, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(2.45, 1.45), group));
		Assert.assertEquals(node3, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(2.85, 2.85), group));
		Assert.assertNull(HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(2.95, 2.95), group));
	}
	
	private SquareNode getSquareNode(VisualNode parent, double x, double y)
	{
		return new SquareNode(null, new Rectangle2D.Double(x, y, 1, 1));
	}
	
	@Test
	public void TestHitSubGroup()
	{
		DefaultStorageManager storage = new DefaultStorageManager();
		VisualGroup root = new VisualGroup(storage);
		
		VisualGroup node1 = new VisualGroup(storage);
		VisualGroup node2 = new VisualGroup(storage);
		root.add(node1);
		root.add((VisualNode)node2);
		node1.add(getSquareNode(node1, 0, 0));
		node2.add(getSquareNode(node2, 1, 1));
		Assert.assertEquals(node2, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(1.5, 1.5), root));
		Assert.assertEquals(node1, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(0.5, 0.5), root));
	}
	
	@Test
	public void TestUngroup()
	{
		DefaultStorageManager storage = new DefaultStorageManager();
		VisualGroup root = new VisualGroup(storage);
		
		VisualGroup node1 = new VisualGroup(storage);
		root.add(node1);
		
		node1.x().setValue(10.0);
		node1.y().setValue(15.0);
		
		VisualGroup node2 = new VisualGroup(storage);
		node1.add(node2);
		
		SquareNode sq1 = getSquareNode(node1, 0, 0);
		node1.add(sq1);
		SquareNode sq2 = getSquareNode(node1, 1, 1);
		node1.add(sq2);
		SquareNode sq3 = getSquareNode(node1, 2, 2);
		node1.add(sq3);

		Assert.assertEquals(sq1, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(10.5, 15.5), node1));
		Assert.assertEquals(sq2, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(11.5, 16.5), node1));
		
		Assert.assertEquals(node1, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(10.5, 15.5), root));
		Assert.assertEquals(node1, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(11.5, 16.5), root));
		Assert.assertEquals(null, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(10.5, 16.5), root));
		
		Iterable<Node> unGroup = node1.unGroup();
		ArrayList<Node> list = new ArrayList<Node>();
		for(Node node: unGroup)
			list.add(node);

		Assert.assertEquals(4, list.size());
		Assert.assertTrue(list.contains(sq1));
		Assert.assertTrue(list.contains(sq2));
		Assert.assertTrue(list.contains(sq3));
		Assert.assertTrue(list.contains(node2));

		Assert.assertTrue(list.indexOf(sq2) > list.indexOf(sq1));
		Assert.assertTrue(list.indexOf(sq3) > list.indexOf(sq2));
		
		Assert.assertNull(HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(0.5, 0.5), node1));
		Assert.assertNull(HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(1.5, 1.5), node1));
		
		Assert.assertEquals(sq1, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(10.5, 15.5), root));
		Assert.assertEquals(sq2, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(11.5, 16.5), root));
		Assert.assertEquals(null, HitMan.hitTestForSelection(TouchableProvider.DEFAULT, new Point2D.Double(10.5, 16.5), root));
	}

	private VisualGroup createGroup(VisualGroup parent)
	{
		return VisualNodeTests.createGroup(parent);
	}
}
