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

package org.workcraft.util;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.NodeHelper;

public class Hierarchy {
	public static Function<Node, Collection<? extends Node>> children = new Function<Node, Collection<? extends Node>>(){
		@Override
		public Collection<? extends Node> apply(Node node) {
			return eval(node.children());
		}
	};
	
	public static <T> Function<Object, Boolean> getTypeFilter(final Class<T> type) {
		return new Function<Object, Boolean> (){
			public Boolean apply(Object node) {
				if (type.isInstance(node))
					return true;
				else
					return false;
			}
		};
	}
	
	public static  Collection<Node> fillterNodes (Collection<Node> nodes, Func<Node, Boolean> filter) {
		LinkedList<Node> result = new LinkedList<Node>();
		
		for (Node node : nodes) {
			if (filter.eval(node))
				result.add(node);
		}
		
		return result;
	}
	
	
	public static <T extends Node> Collection <T> filterNodesByType (Collection<? extends Node> nodes, final Class<T> type) {
		LinkedList<T> result = new LinkedList<T>();
		
		for (Node node : nodes) {
			if (type.isInstance(node))
				result.add(type.cast(node));
		}
		return result;
	}
	public static Node [] getPath(Node node) {
		Node n = node;
			int i = 0;
			while(n!=null)
			{
				i++;
				n = eval(n.parent());
			}
			Node [] result = new Node[i];
			
			n = node;
			while(n!=null)
			{
				result[--i] = n;
				n = eval(n.parent());
			}
			
			return result;
	}
	
	public static Node getCommonParent(Node... nodes) {
		ArrayList<Node[]> paths = new ArrayList<Node[]>(nodes.length);
		
		int minPathLength = Integer.MAX_VALUE;
		
		for (Node node : nodes) {
			final Node[] path = getPath(node);
			if (minPathLength > path.length)
				minPathLength = path.length;
			paths.add(path);
		}
		
		Node result = null;
		
		for(int i=0;i<minPathLength;i++) {
			Node node = paths.get(0)[i];
			
			boolean good = true;
			
			for (Node[] path : paths)
				if (path[i] != node) {
					good = false;
					break;
				}
					
			if (good)
				result = node;
			else
				break;
		}
		
		return result;
	}

	// TODO: eliminate quadratic complexity (using pcollections?)
	static ExpressionBase<List<Node>> getPath(final Expression<? extends Node> node) {
		return new ExpressionBase<List<Node>>() {

			@Override
			public List<Node> evaluate(EvaluationContext resolver) {
				Node n = resolver.resolve(node);
				if(n == null)
					return Collections.<Node>emptyList();
				List<Node> path = new ArrayList<Node>(resolver.resolve(getPath(n.parent())));
				path.add(n);
				return path;
			}
		};
	}
	
	
	
	public static ExpressionBase<Node> getCommonParent(Expression<? extends Node>... nodes) {
		
		final ArrayList<ExpressionBase<List<Node>>> paths = new ArrayList<ExpressionBase<List<Node>>>(nodes.length);
		
		//int minPathLength = Integer.MAX_VALUE;
		
		for (Expression<? extends Node> node : nodes) {
			final ExpressionBase<List<Node>> path = getPath(node);
			//if (minPathLength > path.length)
			//	minPathLength = path.length;
			paths.add(path);
		}
		
		return new ExpressionBase<Node>() {
			
			@Override
			public Node evaluate(EvaluationContext resolver) {
				Node result = null;
				
				ArrayList<List<Node>> ps = new ArrayList<List<Node>>(); 
				for(int i=0;i<paths.size();i++)
					ps.add(resolver.resolve(paths.get(i)));
				
				for(int i=0;;i++) {
					List<Node> first = ps.get(0);
					if(first.size()<=i)
						break;
					
					Node node = first.get(i);
					
					boolean good = true;
					
					for (List<Node> path : ps)
						if(path.size() <= i || path.get(i) != node) {
							good = false;
							break;
						}
							
					if (good)
						result = node;
					else
						break;
				}
				
				return result;
			}				
		};
	}

	public static boolean isDescendant(Node descendant, Node parent) {
		Node node = descendant;
		while(node != parent)
		{
			if(node == null)
				return false;
			node = eval(node.parent());
		}
		return true;
	}
	
	public static Container getNearestContainer (Node... node) {
		Node parent = getCommonParent(node);
		return getNearestAncestor (parent, Container.class);
	}
	
	@SuppressWarnings({ "unchecked" })
	public static <T> T getNearestAncestor(Node node, final Class<T> type)
	{
		return (T)getNearestAncestor(node, new Func<Node, Boolean>()
				{
					@Override
					public Boolean eval(Node node) {
						return type.isInstance(node);
					}
				});		
	}

	public static Node getNearestAncestor(Node node, Func<Node, Boolean> filter) {
		Node parent = node;
		while(parent != null)
		{
			if(filter.eval(parent))
				return parent;
			parent = eval(parent.parent());
		}
		return null;
	}

	public static <T> Collection<T> getChildrenOfType(Node node, Class<T> type)
	{
		return NodeHelper.filterByType(eval(node.children()), type);
	}
	
	public static <T> Collection<T> getDescendantsOfType(Node node, Class<T> type)
	{
		ArrayList<T> result = new ArrayList<T>();
		result.addAll(getChildrenOfType(node, type));
		for(Node n : eval(node.children()))
			result.addAll(getDescendantsOfType(n, type));
		return result;
	}
	
	public static <T> Collection<T> getDescendantsOfType(Node node, Class<T> type, Func<T, Boolean> filter)
	{
		ArrayList<T> result = new ArrayList<T>();
		
		for (T t : getChildrenOfType(node, type))
			if (filter.eval(t))
				result.add(t);
		
		for(Node n : eval(node.children()))
			result.addAll(getDescendantsOfType(n, type, filter));
		
		return result;
	}
	
	public static Collection<Node> getDescendants (Node node) 
	{
		ArrayList<Node> result = new ArrayList<Node>();
		result.addAll(eval(node.children()));
		for(Node n : eval(node.children()))
			result.addAll(getDescendants(n));
		return result;
	}
	
	public static Collection<Node> getDescendants (Node node, Func<Node, Boolean> filter) 
	{
		ArrayList<Node> result = new ArrayList<Node>();
		for(Node n : eval(node.children())) {
			if (filter.eval(n))
				result.add(n);
			result.addAll(getDescendants(n));
		}
		return result;
	}

	public static <T> Expression<Collection<? extends T>> childrenOfType(final Node node, final Class<T> type) {
		return new ExpressionBase<Collection<? extends T>>(){

			@Override
			protected Collection<? extends T> evaluate(EvaluationContext context) {
				return NodeHelper.filterByType(context.resolve(node.children()), type);
			}
		};
	}	 
}
