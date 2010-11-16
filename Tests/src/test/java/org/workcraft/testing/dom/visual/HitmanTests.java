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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.exceptions.NotSupportedException;

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
		}
		public DummyNode(Collection<Node> children)
		{
			this.children = children;
		}
		@Override
		public Expression<? extends Touchable> shape() {
			throw new NotSupportedException();
		}
		@Override
		public ModifiableExpression<Node> parent() {
			throw new NotSupportedException();
		}
		@Override
		public Expression<? extends Collection<? extends Node>> children() {
			return Expressions.constant(children);
		}
	}
	
	class HitableNode extends DummyNode
	{
		@Override
		public Expression<? extends Touchable> shape() {
			return Expressions.constant(new Touchable() {
				@Override
				public boolean hitTest(Point2D point) {
					return true;
				}

				@Override
				public Rectangle2D getBoundingBox() {
					return new Rectangle2D.Double(0, 0, 1, 1);
				}

				@Override
				public Point2D getCenter() {
					return new Point2D.Double(0, 0);
				}
				
			});
		}
	}
	
	@Test
	public void TestHitDeepestSkipNulls()
	{
		final HitableNode toHit = new HitableNode();
		Node node = new DummyNode(
			new Node[]{ 
					new DummyNode(new Node[]{ toHit }),
					new DummyNode(),
			}
		);
		assertSame(toHit, HitMan.hitDeepestNodeOfType(new Point2D.Double(0.5, 0.5), node, HitableNode.class));
	}
}
