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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.util.Func;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class HitMan
{
	private static <T extends Node> Iterable<T> filterByBB(Iterable<T> nodes, final Point2D pointInLocalSpace) {
		return filter(nodes, new Func<T, Boolean>()
				{
			private static final long serialVersionUID = -7790168871113424836L;

			@Override
			public Boolean eval(T arg) {
				if(arg.shape() == null)
					return true;

				Rectangle2D boundingBox = GlobalCache.eval(arg.shape()).getBoundingBox();

				return 
				boundingBox != null &&
				boundingBox.contains(pointInLocalSpace);
			}
			}
		);
	}
	
	private static <T> Iterable<T> filter(Iterable<T> nodes, Func<T, Boolean> filter) {
		ArrayList<T> result = new ArrayList<T>();
		for(T node : nodes)
			if(filter.eval(node))
				result.add(node);
		return result;
	}

	private static Iterable<? extends Node> getFilteredChildren(Point2D point, Node node)
	{
		return reverse(filterByBB(GlobalCache.eval(node.children()), point));
	}

	public static Node hitDeepest(Point2D point, Node node, Func<Node, Boolean> filter) {
		Point2D transformedPoint = transformToChildSpace(point, node);

		for (Node n : getFilteredChildren(transformedPoint, node)) {
			Node result = hitDeepest(transformedPoint, n, filter);
			if(result!=null)
				return result;
		}

		if (filter.eval(node))
			return hitBranch(point, node);
		return null;
	}

	public static boolean isBranchHit (Point2D point, Node node) {

		if (node.shape() != null && GlobalCache.eval(node.shape()).hitTest(point))	{
			if (node instanceof Hidable)
				return !GlobalCache.eval(((Hidable)node).hidden());
			else
				return true;
		}

		Point2D transformedPoint = transformToChildSpace(point, node);

		for (Node n : getFilteredChildren(transformedPoint, node)) {
			if (isBranchHit(transformedPoint, n))
				return true;
		}

		return false;
	}

	public static Node hitFirst(Point2D point, Node node) {
		return hitFirst(point, node, new Func<Node, Boolean>(){
			public Boolean eval(Node arg0) {
				return true;
			}
		});
	}

	public static Node hitFirst(Point2D point, Node node, Func<Node, Boolean> filter) {
		if (filter.eval(node)) {
			return hitBranch(point, node);
		} else {
			return hitFirstChild(point, node, filter);
		}
	}

	private static Node hitBranch(Point2D point, Node node) {
		if(node instanceof CustomTouchable)
			return ((CustomTouchable)node).customHitTest(point);
		
		if (isBranchHit(point, node))
			return node;
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> T hitFirstNodeOfType(Point2D point, Node node, Class<T> type) {
		return (T) hitFirst(point, node, Hierarchy.getTypeFilter(type));
	}

	public static Node hitFirstChild(Point2D point,
			Node node, Func<Node, Boolean> filter) {
		Point2D transformedPoint = transformToChildSpace(point, node);
		for (Node n : getFilteredChildren(transformedPoint, node)) {
			Node hit = hitFirst(transformedPoint, n, filter);
			if (hit != null)
				return hit;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Node> T hitFirstChildOfType(Point2D point, Node node, Class<T> type) {
		return (T) hitFirstChild(point, node, Hierarchy.getTypeFilter(type));
	}

	private static Point2D transformToChildSpace(Point2D point,
			Node node) {
		Point2D transformedPoint; 

		if (node instanceof MovableNew) {
			transformedPoint = new Point2D.Double();
			AffineTransform t = Geometry.optimisticInverse(GlobalCache.eval(((MovableNew)node).transform()));
			t.transform(point, transformedPoint);
		} else
			transformedPoint = point;
		return transformedPoint;
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
	public static <T extends Node> T hitDeepestNodeOfType(Point2D point, Node group, final Class<T> type) {

		return (T)hitDeepest(point, group, Hierarchy.getTypeFilter(type));
	}


	public static Node hitTestForSelection(Point2D point, Node node) {
		Node nd = HitMan.hitFirstChild(point, node, new Func<Node, Boolean>() {
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
			nd = HitMan.hitFirstChild(point, node, new Func<Node, Boolean>() {
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

	public static Node hitTestForConnection(Point2D point, Node node) {
		Node nd = HitMan.hitDeepest(point, node, new Func<Node, Boolean>() {
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
			nd = HitMan.hitDeepest(point, node, new Func<Node, Boolean>() {
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

	public static Node hitTestForConnection (Point2D point, VisualModel model) {
		AffineTransform t = TransformHelper.getTransform(model.getRoot(), eval(model.currentLevel()));
		Point2D pt = new Point2D.Double();
		t.transform(point, pt);
		return hitTestForConnection(pt, eval(model.currentLevel()));	
	}

	public static Node hitTestForSelection (Point2D point, VisualModel model) {
		AffineTransform t = TransformHelper.getTransform(model.getRoot(), eval(model.currentLevel()));
		Point2D pt = new Point2D.Double();
		t.transform(point, pt);
		return hitTestForSelection(pt, eval(model.currentLevel()));	
	}


	/**
	 * The method finds all direct children of the given container, which completely fit inside the given rectangle.
	 * @param container The container whose children will be examined
	 * @param p1 		The top-left corner of the rectangle, in the parent coordinates for the container
	 * @param p2 		The bottom-right corner of the rectangle
	 * @return 			The collection of nodes fitting completely inside the rectangle
	 */
	public static Collection<Node> boxHitTest (Node container, Point2D p1, Point2D p2) {
		
		if(container instanceof MovableNew)
		{
			AffineTransform toLocal = Geometry.optimisticInverse(GlobalCache.eval(((MovableNew) container).transform()));
			toLocal.transform(p1, p1);
			toLocal.transform(p2, p2);
		}

		LinkedList<Node> hit = new LinkedList<Node>();

		Rectangle2D rect = new Rectangle2D.Double(
				Math.min(p1.getX(), p2.getX()), 
				Math.min(p1.getY(), p2.getY()), 
				Math.abs(p1.getX()-p2.getX()), 
				Math.abs(p1.getY()-p2.getY()));

		for (Node n : GlobalCache.eval(container.children())) {
			if(n.shape() != null) {
				if (n instanceof Hidable && GlobalCache.eval(((Hidable)n).hidden()) )
					continue;
	
				if (p1.getX()<=p2.getX()) {
					if (TouchableHelper.insideRectangle(GlobalCache.eval(n.shape()), rect))
						hit.add((Node)n);
				} else {
					if (TouchableHelper.touchesRectangle(GlobalCache.eval(n.shape()), rect))
						hit.add((Node)n);
				}
			}
		}
		return hit;
	}
}