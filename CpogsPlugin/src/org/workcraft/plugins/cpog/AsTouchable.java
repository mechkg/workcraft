package org.workcraft.plugins.cpog;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.visual.BoundedColorisableImage;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.util.Function;
import org.workcraft.util.Maybe;
import org.workcraft.util.MaybeVisitor;

public class AsTouchable {

	public static Function<Node, Expression<Touchable>> instance(final Function<? super Node, ? extends Maybe<? extends Expression<? extends Point2D>>> transform) {
		
		return new Function<Node, Expression<Touchable>>() {
			@Override
			public Expression<Touchable> apply(Node node) {
				final Expression<Touchable> localTouchable = localTouchable(node);
				
				return transform.apply(node).accept(new MaybeVisitor<Expression<? extends Point2D>, Expression<Touchable>>() {
					@Override
					public Expression<Touchable> visitJust(Expression<? extends Point2D> just) {
						return bindFunc(localTouchable, just, TransformHelper.translate());
					}

					@Override
					public Expression<Touchable> visitNothing() {
						return localTouchable;
					}
				});
			}
			
			private Expression<Touchable> localTouchable(Node node) {
				return node.accept(new NodeVisitor<Expression<Touchable>>() {
					@Override
					public Expression<Touchable> visitArc(Arc arc) {
						return null;
					}

					@Override
					public Expression<Touchable> visitComponent(Component component) {
						return AsTouchable.localTouchable(component);
					}
				});
			}
		};
	}
	
	static Function<Rectangle2D, Touchable> bbToTouchable = new Function<Rectangle2D, Touchable>() {

		@Override
		public Touchable apply(final Rectangle2D bb) {
			return new Touchable(){

				@Override
				public boolean hitTest(Point2D point) {
					return getBoundingBox().contains(point);
				}

				@Override
				public Rectangle2D getBoundingBox() {
					return bb;
				}

				@Override
				public Point2D getCenter() {
					return new Point2D.Double(0, 0);
				}
				
			};
		}
	};
	
	private static Expression<Touchable> localTouchable(Component component) {
		return component.accept(new ComponentVisitor<Expression<Touchable>>() {
			private Expression<Touchable> getTouchable(Expression<BoundedColorisableImage> image) {
				return bindFunc(bindFunc(image, BoundedColorisableImage.getBoundingBox), bbToTouchable);
			}

			@Override
			public Expression<Touchable> visitRho(RhoClause rho) {
				Expression<BoundedColorisableImage> image = VisualRhoClause.getVisualRhoClause(rho);
				return getTouchable(image);
			}

			@Override
			public Expression<Touchable> visitVariable(Variable variable) {
				return getTouchable(VisualVariableGui.getImage(variable));
			}

			@Override
			public Expression<Touchable> visitVertex(Vertex vertex) {
				return getTouchable(VisualVertex.getImage(vertex));
			}
		});
	}
}
