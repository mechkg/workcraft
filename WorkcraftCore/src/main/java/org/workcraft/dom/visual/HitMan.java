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

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;
import static org.workcraft.util.Maybe.Util.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.graph.tools.HitTester;
import org.workcraft.util.Function;
import org.workcraft.util.Function0;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Maybe;
import org.workcraft.util.MaybeVisitor;

import pcollections.PCollection;
import pcollections.TreePVector;

public class HitMan
{
	public static class Flat<N> {
		private Instance<N> instance;
		private final Function0<? extends Iterable<? extends N>> contents;

		public Flat(final Function0<? extends Iterable<? extends N>> contents, final Function<? super N, ? extends Touchable> tp) {
			this.contents = contents;
			// we use a special case of hierarchical hit tester with null representing the root and contents representing its children 
			this.instance = new Instance<N>(new Function<N, Iterable<? extends N>>(){
				@Override
				public Iterable<? extends N> apply(N argument) {
					if (argument == null)
						return contents.apply();
					else
						return Collections.emptyList();
				}
			}, new Function<N, Maybe<? extends Touchable>>(){
				@Override
				public Maybe<? extends Touchable> apply(N argument) {
					if(argument == null)
						return nothing();
					else
						return just(tp.apply(argument));
				}
			});
		}
		
		public Maybe<N> hit(Point2D.Double point, Function<? super N, Boolean> filter) {
			return instance.hitFirstChild(point, null, filter);
		}
		
		public HitTester<N> getHitTester() {
			return getHitTester(Arrays.asList(Function.Util.constant(true)));
		}
		
		public HitTester<N> getHitTester(final Iterable<? extends Function<? super N, Boolean>> testers) {
			return new HitTester<N>() {

				@Override
				public Maybe<N> hitTest(Point2D.Double point) {
					for(Function<? super N, Boolean> tester : testers) {
						
						Maybe<N> n = hit(point, tester);
						if(Maybe.Util.isJust(n))
							return n;
					}
					return Maybe.Util.nothing();
				}

				@Override
				public PCollection<N> boxHitTest(Point2D.Double boxStart, Point2D.Double boxEnd) {
					return HitMan.<N>boxHitTest(instance.tp, contents.apply(), boxStart, boxEnd);
				}
				
			};
		}

	}
	
	public static class Instance<N> {
		public Instance(Function<? super N, ? extends Iterable<? extends N>> hierarchy, Function<? super N, ? extends Maybe<? extends Touchable>> tp) {
			this.hierarchy = hierarchy;
			this.tp = tp;
		}

		public final Function<? super N, ? extends Iterable<? extends N>> hierarchy;
		public final Function<? super N, ? extends Maybe<? extends Touchable>> tp;
		
		Iterable<N> filterByBB(Iterable<? extends N> nodes, final Point2D.Double point) {
			return filter(nodes, new Function<N, Boolean>() {
				@Override
				public Boolean apply(N arg) {
					return tp.apply(arg).accept(new MaybeVisitor<Touchable, Boolean>() {
						@Override
						public Boolean visitJust(Touchable touchable) {
							return touchable.getBoundingBox().contains(point);
						}

						@Override
						public Boolean visitNothing() {
							return false;
						}
					});
				}
			});
		}

		private Iterable<? extends N> getFilteredChildren(Point2D.Double point, N node) {
			return reverse(filterByBB(hierarchy.apply(node), point));
		}

		public Maybe<N> hitDeepest(Point2D.Double point, N node, Function<? super N, Boolean> filter) {
			
			for (N n : getFilteredChildren(point, node)) {
				Maybe<N> result = hitDeepest(point, n, filter);
				if(Maybe.Util.isJust(result)) return result;
			}

			if (filter.apply(node))
				return hitBranch(point, node);
			return Maybe.Util.nothing();
		}
		
		private Maybe<N> hitBranch(Point2D.Double point, N node) {
			if (isBranchHit(point, node))
				return Maybe.Util.just(node);
			else
				return Maybe.Util.nothing();
		}
		
		public boolean isBranchHit (Point2D.Double point, N node) {
			Touchable touchable = toNullable(tp.apply(node));
			if (touchable != null && touchable.hitTest(point))	{
				return true;
			}

			for (N n : getFilteredChildren(point, node)) {
				if (isBranchHit(point, n))
					return true;
			}

			return false;
		}
		
		public Maybe<N> hitFirst(Point2D.Double point, N node, Function<? super N, Boolean> filter) {
			if (filter.apply(node)) {
				return hitBranch(point, node);
			} else {
				return hitFirstChild(point, node, filter);
			}
		}

		public Maybe<N> hitFirstChild(Point2D.Double point, N node, Function<? super N, Boolean> filter) {
			for (N n : getFilteredChildren(point, node)) {
				Maybe<N> hit = hitFirst(point, n, filter);
				
				if (Maybe.Util.isJust(hit))
					return hit;
			}
			return Maybe.Util.nothing();
		}

		public Maybe<N> hitFirst(Point2D.Double point, N node) {
			return hitFirst(point, node, new Function<N, Boolean>(){
				public Boolean apply(N arg0) {
					return true;
				}
			});
		}

		/**
		 * Deprecated to discourage the use of reflection
		 */
		@Deprecated
		public <T> T hitFirstNodeOfType(Point2D.Double point, N node, Class<T> type) {
			return type.cast(hitFirst(point, node, Hierarchy.getTypeFilter(type)));
		}

		/**
		 * Deprecated to discourage the use of reflection
		 */
		@Deprecated
		public <T> T hitFirstChildOfType(Point2D.Double point, N node, Class<T> type) {
			return type.cast(hitFirstChild(point, node, Hierarchy.getTypeFilter(type)));
		}
		
		/**
		 * Deprecated to discourage the use of reflection
		 */
		@Deprecated
		public <T> T hitDeepestNodeOfType(Point2D.Double point, N group, final Class<T> type) {
			return type.cast(hitDeepest(point, group, Hierarchy.getTypeFilter(type)));
		}
	}
	
	private static <T> Iterable<T> filter(Iterable<? extends T> nodes, Function<? super T, Boolean> filter) {
		ArrayList<T> result = new ArrayList<T>();
		for(T node : nodes)
			if(filter.apply(node))
				result.add(node);
		return result;
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

	public static <N extends Node> Maybe<N> hitTestForSelection(Function<? super N, ? extends Maybe<? extends Touchable>> tp, Point2D.Double point, N node, final Class<N> type) {
		Function<N, Iterable<? extends N>> children = new Function<N, Iterable<? extends N>>() {
			@Override
			public Iterable<? extends N> apply(N argument) {
				ArrayList<N> result = new ArrayList<N>();
				for(Node n : eval(argument.children()))
					result.add(type.cast(n));
				return result;
			}
		};
		
		Instance<N> hitMan = new Instance<N>(children, tp);
		
		Maybe<N> nd = hitMan.hitFirstChild(point, node, new Function<N, Boolean>() {
			@Override
			public Boolean apply(N n) {
				return n instanceof MovableNew;
			}
		});

		if (Maybe.Util.isJust(nd))
			nd = hitMan.hitFirstChild(point, node, new Function<Node, Boolean>() {
				public Boolean apply(Node n) {
					return n instanceof VisualConnection;
				}
			});

		return nd;
	}

	public static Maybe<Node> hitTestForConnection(Function<? super Node, ? extends Maybe<? extends Touchable>> tp, Point2D.Double point, Node node) {
		Instance<Node> hitMan = new Instance<Node>(Hierarchy.children, tp);
		
		Maybe<Node> nd = hitMan.hitDeepest(point, node, new Function<Node, Boolean>() {
			public Boolean apply(Node n) {
				return n instanceof MovableNew && ! (n instanceof Container);
			}
		});

		if (Maybe.Util.isJust(nd))
			nd = hitMan.hitDeepest(point, node, new Function<Node, Boolean>() {
				public Boolean apply(Node n) {
					return n instanceof VisualConnection;
				}
			});

		return nd;
	}

	/**
	 * The method finds all direct children of the given container, which completely fit inside the given rectangle.
	 * @param container The container whose children will be examined
	 * @param p1 		The top-left corner of the rectangle, in the parent coordinates for the container
	 * @param p2 		The bottom-right corner of the rectangle
	 * @return 			The collection of nodes fitting completely inside the rectangle
	 */
	public static <N> PCollection<N> boxHitTest (Function<? super N, ? extends Maybe<? extends Touchable>> t, Iterable<? extends N> nodes, Point2D.Double p1, Point2D.Double p2) {
		PCollection<N> hit = TreePVector.<N>empty();

		Rectangle2D.Double rect = Geometry.createRectangle(p1, p2);

		for (N n : nodes) {
			Touchable touchable = Maybe.Util.toNullable(t.apply(n));
			if(touchable != null) {
				if (p1.getX()<=p2.getX()) {
					if (TouchableHelper.insideRectangle(touchable, rect))
						hit = hit.plus(n);
				} else {
					if (TouchableHelper.touchesRectangle(touchable, rect))
						hit = hit.plus(n);
				}
			}
		}
		return hit;
	}
}
