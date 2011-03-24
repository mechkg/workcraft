package org.workcraft.gui.graph.tools;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.util.Maybe.Util.*;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.visual.AffineTransform_Position;
import org.workcraft.dom.visual.MovableNew;
import org.workcraft.util.Function;
import org.workcraft.util.Geometry;
import org.workcraft.util.Maybe;
import org.workcraft.util.TwoWayFunction;

public interface MovableController<Node> {
	Maybe<ModifiableExpression<Point2D>> position(Node node);
	
	MovableController<org.workcraft.dom.Node> REFLECTIVE = new MovableController<org.workcraft.dom.Node>() {
		@Override
		public Maybe<ModifiableExpression<Point2D>> position(org.workcraft.dom.Node node) {
			if(node instanceof MovableNew) {
				ModifiableExpression<Point2D> result = new AffineTransform_Position(((MovableNew)node).transform());
				return just(result);
			} else
				return nothing();
		}
	};
	
	Function<org.workcraft.dom.Node, Expression<Point2D>> TRANSFORM_PROVIDER = new Function<org.workcraft.dom.Node, Expression<Point2D>>(){
		@Override
		public Expression<Point2D> apply(final org.workcraft.dom.Node node) {
			if(node == null) {
				Point2D result = new Point2D.Double(0,0);
				return constant(result);
			}
			return orElse(REFLECTIVE_HIERARCHICAL.position(node), bind(node.parent(), TRANSFORM_PROVIDER));
		}
	};
	
	MovableController<org.workcraft.dom.Node> REFLECTIVE_HIERARCHICAL = new MovableController<org.workcraft.dom.Node>() {
		@Override
		public Maybe<ModifiableExpression<Point2D>> position(final org.workcraft.dom.Node node) {

			if(node == null)
				return nothing();
			final Maybe<ModifiableExpression<Point2D>> position = REFLECTIVE.position(node);
			
			return applyFunc(position, new Function<ModifiableExpression<Point2D>, ModifiableExpression<Point2D>>(){
				@Override
				public ModifiableExpression<Point2D> apply(ModifiableExpression<Point2D> position) {
					final Expression<Point2D> transform = bind(node.parent(), TRANSFORM_PROVIDER);
					return bind(position, transform, new Function<Point2D, TwoWayFunction<Point2D, Point2D>>(){
						@Override
						public TwoWayFunction<Point2D, Point2D> apply(final Point2D shift) {
							return Geometry.addFunc(shift);
						}
					});
				}
			});
		}
	};

	public class Util {

		public static <Node>MovableController<Node> empty() {
			return new MovableController<Node>(){
				@Override
				public Maybe<ModifiableExpression<Point2D>> position(Node node) {
					return nothing();
				}
			};
		}
		
	}
}
