package org.workcraft.gui.graph.tools;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.Combinator;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.core.ModifiableExpressionCombinator;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.visual.AffineTransform_Position;
import org.workcraft.dom.visual.MovableNew;
import org.workcraft.util.ExpressionUtil;
import org.workcraft.util.Function;
import org.workcraft.util.Geometry;

import checkers.nullness.quals.Nullable;

public interface MovableController<Node> {
	@Nullable ModifiableExpression<Point2D> position(Node node);
	
	MovableController<org.workcraft.dom.Node> REFLECTIVE = new MovableController<org.workcraft.dom.Node>() {
		@Override
		public @Nullable ModifiableExpression<Point2D> position(org.workcraft.dom.Node node) {
			if(node instanceof MovableNew) 
				return new AffineTransform_Position(((MovableNew)node).transform());
			else
				return null;
		}
	};
	
	public class Util {
		public static <T> Combinator<T, Point2D> asCombinator(final MovableController<T> controller) {
			return new Combinator<T, Point2D>() {
				@Override
				public Expression<Point2D> apply(T arg) {
					return controller.position(arg);
				}
			};
		}
	}
	
	MovableController<org.workcraft.dom.Node> REFLECTIVE_HIERARCHICAL = new MovableController<org.workcraft.dom.Node>() {
		@Override
		public @Nullable ModifiableExpression<Point2D> position(org.workcraft.dom.Node node) {

			if(node == null)
				return ExpressionUtil.modificationNotSupported(Expressions.<Point2D>constant(new Point2D.Double(0,0)));
			
			final Expression<? extends Point2D> parentTransform = Expressions.bind(node.parent(), Util.asCombinator(REFLECTIVE_HIERARCHICAL));
			
			return Expressions.bind(REFLECTIVE.position(node), new ModifiableExpressionCombinator<Point2D, Point2D>(){
				@Override
				public Expression<? extends Point2D> get(final Point2D localPosition) {
					return Expressions.bindFunc(parentTransform, new Function<Point2D, Point2D>(){
						@Override
						public Point2D apply(Point2D parentPosition) {
							return Geometry.add(localPosition, parentPosition);
						}
					});
				}

				@Override
				public Point2D set(Point2D oldVal, Point2D newVal) {
					return Geometry.subtract(newVal, GlobalCache.eval(parentTransform));
				}
			});
		}
	};
}
