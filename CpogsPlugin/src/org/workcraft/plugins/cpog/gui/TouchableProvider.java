package org.workcraft.plugins.cpog.gui;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.plugins.cpog.Arc;
import org.workcraft.plugins.cpog.Component;
import org.workcraft.plugins.cpog.ComponentVisitor;
import org.workcraft.plugins.cpog.Node;
import org.workcraft.plugins.cpog.NodeVisitor;
import org.workcraft.plugins.cpog.RhoClause;
import org.workcraft.plugins.cpog.Variable;
import org.workcraft.plugins.cpog.Vertex;
import org.workcraft.plugins.cpog.VisualRhoClause;
import org.workcraft.plugins.cpog.VisualVariableGui;
import org.workcraft.plugins.cpog.VisualVertex;
import org.workcraft.util.Function;

public class TouchableProvider {

	public static <N> Function<N, Expression<Touchable>> transform
			( final Function<? super N, ? extends Expression<? extends Touchable>> localTouchable
			, final Function<? super N, ? extends Expression<? extends Point2D>> transform
			) 
	{
		return new Function<N, Expression<Touchable>>() {
			@Override
			public Expression<Touchable> apply(N node) {
				return fmap(TransformHelper.translate(), localTouchable.apply(node), transform.apply(node));
			}
		};
	}

	public static Expression<Touchable> localTouchable(Node node) {
		return node.accept(new NodeVisitor<Expression<Touchable>>() {
			@Override
			public Expression<Touchable> visitArc(Arc arc) {
				throw new NotImplementedException();
			}

			@Override
			public Expression<Touchable> visitComponent(Component component) {
				return localTouchable(component);
			}
		});
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
	
	public static Function<Node, Expression<Touchable>> nodeLocalTouchable = new Function<Node, Expression<Touchable>>() {
		@Override
		public Expression<Touchable> apply(Node argument) {
			return localTouchable(argument);
		}
	};
	
	public static Function<Component, Expression<Touchable>> componentLocalTouchable = new Function<Component, Expression<Touchable>>() {
		@Override
		public Expression<Touchable> apply(Component argument) {
			return localTouchable(argument);
		}
	};
	
	private static Expression<Touchable> localTouchable(Component component) {
		return component.accept(new ComponentVisitor<Expression<Touchable>>() {
			private Expression<Touchable> getTouchable(Expression<BoundedColorisableGraphicalContent> image) {
				return fmap(bbToTouchable, fmap(BoundedColorisableGraphicalContent.getBoundingBox, image));
			}

			@Override
			public Expression<Touchable> visitRho(RhoClause rho) {
				Expression<BoundedColorisableGraphicalContent> image = VisualRhoClause.getVisualRhoClause(rho);
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
