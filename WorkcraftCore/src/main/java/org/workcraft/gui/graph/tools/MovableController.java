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

public interface MovableController<Node> extends Function<Node, Maybe<? extends ModifiableExpression<Point2D.Double>>> {
	
	MovableController<org.workcraft.dom.Node> REFLECTIVE = new MovableController<org.workcraft.dom.Node>() {
		@Override
		public Maybe<ModifiableExpression<Point2D.Double>> apply(org.workcraft.dom.Node node) {
			if(node instanceof MovableNew) {
				ModifiableExpression<Point2D.Double> result = new AffineTransform_Position(((MovableNew)node).transform());
				return just(result);
			} else
				return nothing();
		}
	};
	
	Function<org.workcraft.dom.Node, Expression<Point2D.Double>> TRANSFORM_PROVIDER = new Function<org.workcraft.dom.Node, Expression<Point2D.Double>>(){
		@Override
		public Expression<Point2D.Double> apply(final org.workcraft.dom.Node node) {
			if(node == null) {
				Point2D.Double result = new Point2D.Double(0,0);
				return constant(result);
			}
			return orElse(REFLECTIVE_HIERARCHICAL.apply(node), bind(node.parent(), TRANSFORM_PROVIDER));
		}
	};
	
	MovableController<org.workcraft.dom.Node> REFLECTIVE_HIERARCHICAL = new MovableController<org.workcraft.dom.Node>() {
		@Override
		public Maybe<ModifiableExpression<Point2D.Double>> apply(final org.workcraft.dom.Node node) {

			if(node == null)
				return nothing();
			final Maybe<? extends ModifiableExpression<Point2D.Double>> position = REFLECTIVE.apply(node);
			
			return applyFunc(position, new Function<ModifiableExpression<Point2D.Double>, ModifiableExpression<Point2D.Double>>(){
				@Override
				public ModifiableExpression<Point2D.Double> apply(ModifiableExpression<Point2D.Double> position) {
					final Expression<Point2D.Double> transform = bind(node.parent(), TRANSFORM_PROVIDER);
					return applyM(position, fmap(new Function<Point2D.Double, TwoWayFunction<Point2D.Double, Point2D.Double>>(){
						@Override
						public TwoWayFunction<Point2D.Double, Point2D.Double> apply(final Point2D.Double shift) {
							return Geometry.addFunc(shift);
						}
					}, transform));
				}
			});
		}
	};

	public class Util {

		public static <Node>MovableController<Node> empty() {
			return new MovableController<Node>(){
				@Override
				public Maybe<ModifiableExpression<Point2D.Double>> apply(Node node) {
					return nothing();
				}
			};
		}
		
	}
}
