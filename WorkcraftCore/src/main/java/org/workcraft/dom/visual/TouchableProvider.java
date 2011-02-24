package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.Combinator;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Node;
import org.workcraft.gui.graph.tools.MovableController;
import org.workcraft.util.Function2;
import org.workcraft.util.Geometry;

public interface TouchableProvider<N> extends Combinator<N,Touchable> {
	class WithTranslations implements TouchableProvider<Node> {
		MovableController<Node> movable = MovableController.REFLECTIVE_HIERARCHICAL;
		private final Node root;

		public WithTranslations(Node root) {
			this.root = root;
		}

		@Override
		public Expression<? extends Touchable> apply(Node node) {
			Expression<? extends Touchable> localTouchable = REFLECTIVE.apply(node);
			ModifiableExpression<Point2D> position = movable.position(node);
			ModifiableExpression<Point2D> rootPosition = movable.position(root);
			
			Expression<? extends Point2D> relativePosition = Expressions.bindFunc(position, rootPosition, new Function2<Point2D, Point2D, Point2D>(){
				@Override
				public Point2D apply(Point2D position, Point2D origin) {
					return Geometry.subtract(position, origin);
				}
			});
			
			return Expressions.bindFunc(localTouchable, relativePosition, new Function2<Touchable, Point2D, Touchable>() {
				@Override
				public Touchable apply(Touchable arg1, Point2D arg2) {
					return new TouchableTransformer(arg1, AffineTransform.getTranslateInstance(arg2.getX(), arg2.getY()));
				}
			});
		}
	}

	TouchableProvider<Node> REFLECTIVE = new TouchableProvider<Node>(){
		@Override
		public Expression<? extends Touchable> apply(Node node) {
			if (node instanceof ReflectiveTouchable) 
				return ((ReflectiveTouchable)node).shape();
			else if(node instanceof VisualGroup)
				return VisualGroup.localSpaceTouchable(new WithTranslations(node), ((VisualGroup)node));
			else
				return null;
		}
	};
	
	TouchableProvider<Node> REFLECTIVE_WITH_TRANSLATIONS = new WithTranslations(null);
}
