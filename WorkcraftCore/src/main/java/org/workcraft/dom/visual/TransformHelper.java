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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.Node;
import org.workcraft.util.Function2;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;

public class TransformHelper {

	public static void applyTransform(Node node, AffineTransform transform) {
		if(node instanceof MovableNew)
			MovableHelper.applyTransform(((MovableNew) node), transform);
		else
			applyTransformToChildNodes(node, transform);
	}

	public static void applyTransformToChildNodes(Node node, AffineTransform transform) {
		for(Node n: eval(node.children()))
			applyTransform(n, transform);
	}

	public static AffineTransform getTransformToAncestor(Node node, Node ancestor) {
		AffineTransform t = new AffineTransform();
		
		while (ancestor != node) {
			Node next = eval(node.parent()); 
			if (next == null)
				throw new NotAnAncestorException(node, ancestor);
			if(next instanceof MovableNew)
				t.preConcatenate(GlobalCache.eval(((MovableNew)next).transform()));
			node = next;
		}
		
		return t;
	}

	public static AffineTransform getTransform(Node node1, Node node2) {
		Node parent = Hierarchy.getCommonParent(node1, node2);
		if(parent == null)
			throw new RuntimeException(String.format("Nodes '%s' and '%s' do not have a common parent", node1, node2));
		AffineTransform node1ToParent = getTransformToAncestor(node1, parent);
		AffineTransform node2ToParent = getTransformToAncestor(node2, parent);
		AffineTransform parentToNode2 = Geometry.optimisticInverse(node2ToParent);
		
		parentToNode2.concatenate(node1ToParent);
		return parentToNode2;
	}
	
	public static Touchable transform(Touchable touchable, AffineTransform transform)
	{
		return new TouchableTransformer(touchable, transform);
	}

	public static Expression<AffineTransform> getTransformToAncestor(final Expression<? extends Node> node, final Expression<? extends Node> ancestor) {
		return new ExpressionBase<AffineTransform>() {
			
			@Override
			public AffineTransform evaluate(EvaluationContext resolver) {
				
				AffineTransform t = new AffineTransform();
				Node nodeInQuestion = resolver.resolve(node);
				Node tmp = nodeInQuestion;
				Node ancestorNode = resolver.resolve(ancestor);
				while (ancestorNode != tmp) {
					Node next = resolver.resolve(tmp.parent()); 
					if (next == null)
						throw new NotAnAncestorException(nodeInQuestion, ancestorNode);
					if(next instanceof MovableNew)
						t.preConcatenate(resolver.resolve(((MovableNew)next).transform()));
					tmp = next;
				}
				
				return t;
			};
		};
	}	
	
	@SuppressWarnings("unchecked")
	public static Expression<AffineTransform> getTransformExpression(Expression<? extends Node> node1, Expression<? extends Node> node2) {
		final Expression<? extends Node> parent = Hierarchy.getCommonParent(node1, node2);
		final Expression<? extends AffineTransform> node1ToParent = getTransformToAncestor(node1, parent);
		final Expression<? extends AffineTransform> node2ToParent = getTransformToAncestor(node2, parent);
		return new ExpressionBase<AffineTransform>() {
			@Override
			public AffineTransform evaluate(EvaluationContext resolver) {
				AffineTransform parentToNode2 = Geometry.optimisticInverse(resolver.resolve(node2ToParent));
				parentToNode2.concatenate(resolver.resolve(node1ToParent));
				return parentToNode2;
			}
		};
	}

	public static ExpressionBase<Touchable> transform(final Expression<? extends Touchable> node, final Expression<? extends AffineTransform> transform) {
		return new ExpressionBase<Touchable>() {
			@Override
			public Touchable evaluate(EvaluationContext resolver) {
				return new TouchableTransformer(resolver.resolve(node), resolver.resolve(transform));
			}
		};
	}

	public static Function2<AffineTransform, Touchable, Touchable> transform() {
		return new Function2<AffineTransform, Touchable, Touchable>() {
			@Override
			public Touchable apply(AffineTransform transform, Touchable touchable) {
				return transform(touchable, transform);
			}
		};
	}

	public static Function2<? super Touchable, ? super Point2D, ? extends Touchable> translate() {
		return new Function2<Touchable, Point2D, Touchable>(){
			@Override
			public Touchable apply(Touchable touchable, Point2D offset) {
				return transform(touchable, AffineTransform.getTranslateInstance(offset.getX(), offset.getY()));
			}
		};
	}
}
