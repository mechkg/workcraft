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

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.util.Func;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;

import pcollections.PCollection;
import pcollections.TreePVector;

public class HitMan
{
	private static <T extends Node> Iterable<T> filterByBB(final TouchableProvider<? super T> tp, Iterable<? extends T> nodes, final Point2D point) {
		return filter(nodes, new Func<T, Boolean>()
				{
			private static final long serialVersionUID = -7790168871113424836L;

			@Override
			public Boolean eval(T arg) {
				if(tp.apply(arg) == null)
					return true;
				if(arg == null)
					throw new RuntimeException("wtf");

				Rectangle2D boundingBox = GlobalCache.eval(tp.apply(arg)).getBoundingBox();

				return 
				boundingBox != null &&
				boundingBox.contains(point);
			}
			}
		);
	}
	
	private static <T> Iterable<T> filter(Iterable<? extends T> nodes, Func<? super T, Boolean> filter) {
		ArrayList<T> result = new ArrayList<T>();
		for(T node : nodes)
			if(filter.eval(node))
				result.add(node);
		return result;
	}

	private static Iterable<? extends Node> getFilteredChildren(final TouchableProvider<Node> tp, Point2D point, Node node)
	{
		return reverse(filterByBB(tp, GlobalCache.eval(node.children()), point));
	}

	public static Node hitDeepest(final TouchableProvider<Node> tp, Point2D point, Node node, Func<Node, Boolean> filter) {
		
		for (Node n : getFilteredChildren(tp, point, node)) {
			Node result = hitDeepest(tp, point, n, filter);
			if(result!=null)
				return result;
		}

		if (filter.eval(node))
			return hitBranch(tp, point, node);
		return null;
	}

	public static boolean isBranchHit (final TouchableProvider<Node> tp, Point2D point, Node node) {

		Expression<? extends Touchable> touchable = tp.apply(node);
		if (touchable != null && GlobalCache.eval(touchable).hitTest(point))	{
			if (node instanceof Hidable)
				return !GlobalCache.eval(((Hidable)node).hidden());
			else
				return true;
		}

		for (Node n : getFilteredChildren(tp, point, node)) {
			if (isBranchHit(tp, point, n))
				return true;
		}

		return false;
	}

	public static Node hitFirst(final TouchableProvider<Node> tp, Point2D point, Node node) {
		return hitFirst(tp, point, node, new Func<Node, Boolean>(){
			public Boolean eval(Node arg0) {
				return true;
			}
		});
	}

	public static Node hitFirst(final TouchableProvider<Node> tp, Point2D point, Node node, Func<Node, Boolean> filter) {
		if (filter.eval(node)) {
			return hitBranch(tp, point, node);
		} else {
			return hitFirstChild(tp, point, node, filter);
		}
	}

	private static Node hitBranch(final TouchableProvider<Node> tp, Point2D point, Node node) {
		if(node instanceof CustomTouchable) {
			if(true)throw new NotImplementedException("Get rid of CustomTouchable in favor of custom hitman");
			return ((CustomTouchable)node).customHitTest(point);
		}
		
		if (isBranchHit(tp, point, node))
			return node;
		else
			return null;
	}

	public static <T extends Node> T hitFirstNodeOfType(TouchableProvider<Node> tp, Point2D point, Node node, Class<T> type) {
		return type.cast(hitFirst(tp, point, node, Hierarchy.getTypeFilter(type)));
	}

	public static Node hitFirstChild(TouchableProvider<Node> tp, Point2D point, Node node, Func<Node, Boolean> filter) {
		for (Node n : getFilteredChildren(tp, point, node)) {
			Node hit = hitFirst(tp, point, n, filter);
			if (hit != null)
				return hit;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> T hitFirstChildOfType(TouchableProvider<Node> tp, Point2D point, Node node, Class<T> type) {
		return (T) hitFirstChild(tp, point, node, Hierarchy.getTypeFilter(type));
	}

	private static <T> Iterable<T> reverse(Iterable<T> original)
	{
		final ArrayList<T> list = new ArrayList<T>(); 
		for (T node : original)
			list.add(node);
		return new Iterable<T>()
		{
			public Iterator<T> iterator() {
				return new Iterator<T>()
				{
					private int cur = list.size();
					public boolean hasNext() {
						return cur>0;
					}
					public T next() {
						return list.get(--cur);
					}
					public void remove() {
						throw new RuntimeException("Not supported");
					}
				};
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> T hitDeepestNodeOfType(TouchableProvider<Node> tp, Point2D point, Node group, final Class<T> type) {
		return (T)hitDeepest(tp, point, group, Hierarchy.getTypeFilter(type));
	}

	public static Node hitTestForSelection(TouchableProvider<Node> tp, Point2D point, Node node) {
		Node nd = HitMan.hitFirstChild(tp, point, node, new Func<Node, Boolean>() {
			public Boolean eval(Node n) {
				if (!(n instanceof MovableNew))
					return false;

				if (n instanceof Hidable)
					return !GlobalCache.eval(((Hidable)n).hidden());
				else
					return true;
			}
		});

		if (nd == null)
			nd = HitMan.hitFirstChild(tp, point, node, new Func<Node, Boolean>() {
				public Boolean eval(Node n) {
					if (n instanceof VisualConnection) {
						if (n instanceof Hidable) 
							return !GlobalCache.eval(((Hidable)n).hidden());
						else
							return true;
					}
					else
						return false;
				}
			});

		return nd;
	}

	public static Node hitTestForConnection(TouchableProvider<Node> tp, Point2D point, Node node) {
		Node nd = HitMan.hitDeepest(tp, point, node, new Func<Node, Boolean>() {
			public Boolean eval(Node n) {
				if (n instanceof MovableNew && ! (n instanceof Container)) {
					if (n instanceof Hidable) 
						return !GlobalCache.eval(((Hidable)n).hidden());
					else
						return true;					
				}
				else
					return false;
			}
		});

		if (nd == null)
			nd = HitMan.hitDeepest(tp, point, node, new Func<Node, Boolean>() {
				public Boolean eval(Node n) {
					if (n instanceof VisualConnection) {
						if (n instanceof Hidable) 
							return !GlobalCache.eval(((Hidable)n).hidden());
						else
							return true;
					}
					else
						return false;
				}
			});

		return nd;
	}

	public static PCollection<Node> boxHitTest (TouchableProvider<Node> touchableProvider, Node container, Point2D p1, Point2D p2) {
		return boxHitTest(touchableProvider, eval(container.children()), p1, p2);
	}

	/**
	 * The method finds all direct children of the given container, which completely fit inside the given rectangle.
	 * @param container The container whose children will be examined
	 * @param p1 		The top-left corner of the rectangle, in the parent coordinates for the container
	 * @param p2 		The bottom-right corner of the rectangle
	 * @return 			The collection of nodes fitting completely inside the rectangle
	 */
	public static <N> PCollection<N> boxHitTest (TouchableProvider<? super N> t, Collection<? extends N> nodes, Point2D p1, Point2D p2) {
		PCollection<N> hit = TreePVector.<N>empty();

		Rectangle2D rect = Geometry.createRectangle(p1, p2);

		for (N n : nodes) {
			Expression<? extends Touchable> tt = t.apply(n);
			if(tt != null) {
				Touchable touchable = eval(tt);
				if (p1.getX()<=p2.getX()) {
					if (TouchableHelper.insideRectangle(touchable, rect))
						hit.plus(n);
				} else {
					if (TouchableHelper.touchesRectangle(touchable, rect))
						hit.plus(n);
				}
			}
		}
		return hit;
	}
}
