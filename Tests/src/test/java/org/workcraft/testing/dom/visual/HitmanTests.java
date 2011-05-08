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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.ReflectiveTouchable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TouchableProvider;

import pcollections.PCollection;

public class HitmanTests {
	class DummyNode implements Node
	{
		Collection<Node> children;
		public DummyNode()
		{
			children = Collections.emptyList();
		}
		public DummyNode(Node[] children)
		{
			this.children = new ArrayList<Node>(Arrays.asList(children));
			for(Node node : children)
				node.parent().setValue(this);
		}
		public DummyNode(Collection<Node> children)
		{
			this.children = children;
		}
		Variable<Node> parent = Variable.create(null);
		@Override
		public ModifiableExpression<Node> parent() {
			return parent;
		}
		@Override
		public Expression<? extends Collection<? extends Node>> children() {
			return Expressions.constant(children);
		}
	}
	
	class HitableNode extends DummyNode implements ReflectiveTouchable
	{
		private final Point2D point;

		public HitableNode(Point2D point) {
			this.point = point;
		}

		public HitableNode() {
			this(new Point2D.Double(0, 0));
		}
		
		@Override
		public Expression<? extends Touchable> shape() {
			return Expressions.constant(new Touchable() {
				@Override
				public boolean hitTest(Point2D point) {
					return true;
				}

				@Override
				public Rectangle2D getBoundingBox() {
					return new Rectangle2D.Double(point.getX(), point.getY(), 1, 1);
				}

				@Override
				public Point2D getCenter() {
					return point;
				}
				
			});
		}
	}
	
	@Test
	public void testHitDeepestSkipNulls()
	{
		final HitableNode toHit = new HitableNode();
		Node node = new DummyNode(
			new Node[]{ 
					new DummyNode(new Node[]{ toHit }),
					new DummyNode(),
			}
		);
		assertSame(toHit, HitMan.hitDeepestNodeOfType(TouchableProvider.DEFAULT, new Point2D.Double(0.5, 0.5), node, HitableNode.class));
	}
	
	@Test
	public void testBoxHitTest() {
		final HitableNode toHit1 = new HitableNode(new Point2D.Double(0,0));
		final HitableNode toHit2 = new HitableNode(new Point2D.Double(1,1));
		final HitableNode toHit3 = new HitableNode(new Point2D.Double(2,2));
		List<HitableNode> nodes = Arrays.asList(toHit1, toHit2, toHit3);
		
		assertCollectionsEqual(Arrays.asList(toHit1, toHit2), HitMan.boxHitTest(TouchableProvider.LOCAL_REFLECTIVE, nodes, new Point2D.Double(-0.5, -0.5), new Point2D.Double(2.5, 2.5)));
		
	}

	private void assertCollectionsEqual(List<HitableNode> asList, PCollection<HitableNode> boxHitTest) {
		assertSubSet(asList, boxHitTest);
		assertSubSet(boxHitTest, asList);
	}

	private void assertSubSet(Collection<HitableNode> subset, Collection<HitableNode> superset) {
		HashSet<HitableNode> ht = new HashSet<HitableNode>(subset);
		ht.removeAll(superset);
		assertTrue(ht.isEmpty());
	}
}
