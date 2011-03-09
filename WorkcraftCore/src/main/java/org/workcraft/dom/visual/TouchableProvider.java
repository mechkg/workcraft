package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.Combinator;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.ConnectionGraphicConfiguration;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnectionGui;
import org.workcraft.gui.graph.tools.MovableController;
import org.workcraft.util.Function2;

public interface TouchableProvider<N> extends Combinator<N,Touchable> {
	
	public final TouchableProvider<Node> LOCAL_REFLECTIVE = new TouchableProvider<Node> () {
		@Override
		public Expression<? extends Touchable> apply(Node node) {
			if (node instanceof ReflectiveTouchable) 
				return ((ReflectiveTouchable)node).shape();
			else
				return null;
		}
	};
	
	TouchableProvider<Node> DEFAULT = Util.applyTransformAndAddVisualGroupsAndConnections(LOCAL_REFLECTIVE);
	
	public final class Util{
		public static TouchableProvider<Node> applyTransformAndAddVisualGroupsAndConnections(final TouchableProvider<Node> localTP) {
			final MovableController<Node> movable = MovableController.REFLECTIVE_HIERARCHICAL;
			return new TouchableProvider<Node>() {
				@Override
				public Expression<? extends Touchable> apply(Node node) {

					final Expression<? extends Touchable> localTouchable;
					
					if(node instanceof VisualGroup)
						return VisualGroup.screenSpaceBounds(this, (VisualGroup)node);
					else
						if(node instanceof ConnectionGraphicConfiguration) 
							return VisualConnectionGui.getShape(this, (VisualConnection)GlobalCache.eval((node.parent())), (ConnectionGraphicConfiguration)node);
						localTouchable = localTP.apply(node);
					
					if(localTouchable == null) // fuck you java :D
						return null;
					ModifiableExpression<Point2D> position = movable.position(node);
					
					if(position == null)
						return localTouchable;
					
					return Expressions.bindFunc(localTouchable, position, new Function2<Touchable, Point2D, Touchable>() {
						@Override
						public Touchable apply(Touchable arg1, Point2D arg2) {
							return new TouchableTransformer(arg1, AffineTransform.getTranslateInstance(arg2.getX(), arg2.getY()));
						}
					});
				}
				
			};
		}
	}
}
