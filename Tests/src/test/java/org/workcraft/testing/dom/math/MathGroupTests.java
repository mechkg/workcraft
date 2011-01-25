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

package org.workcraft.testing.dom.math;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.util.Collection;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.stg.DefaultStorageManager;

public class MathGroupTests {
	class MockNode extends MathNode {

		public MockNode(StorageManager storage) {
			super(storage);
			
		}

	}

	private MockNode n1 = new MockNode(new DefaultStorageManager());
	private MockNode n2 = new MockNode(new DefaultStorageManager());

	private MathGroup group;

	@Test
	public void ObservationTest() {
		group = new MathGroup(new DefaultStorageManager());

		Expression<? extends Collection<Node>> children = identity((Expression<? extends Collection<Node>>) group.children());

		group.add(n1);
		assertTrue(eval(children).contains(n1));
		group.add(n2);
		assertTrue(eval(children).contains(n2));

		assertTrue(eval(children).size() == 2);

		group.remove(n2);
		assertFalse(eval(children).contains(n2));
		group.remove(n1);
		assertFalse(eval(children).contains(n1));

		assertTrue(eval(children).size() == 0);
	}

	private <T> Expression<T> identity(final Expression<T> original) {
		return new ExpressionBase<T>() {
			@Override
			protected T evaluate(EvaluationContext context) {
				return context.resolve(original);
			}
		};
	}
}
