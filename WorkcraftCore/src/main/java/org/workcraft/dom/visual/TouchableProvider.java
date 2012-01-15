package org.workcraft.dom.visual;

import static org.workcraft.dependencymanager.advanced.core.Expressions.fmap;
import static org.workcraft.dependencymanager.advanced.core.Expressions.joinFunction;
import static org.workcraft.util.Function.Util.curry;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.gui.graph.tools.MovableController;
import org.workcraft.util.Function;
import org.workcraft.util.Function2;
import org.workcraft.util.Maybe;


public interface TouchableProvider<N> extends Function<N, Expression<? extends Maybe<? extends Touchable>>> {
	
	public final TouchableProvider<Node> LOCAL_REFLECTIVE = new TouchableProvider<Node> () {
		@Override
		public Expression<? extends Maybe<Touchable>> apply(Node node) {
			if (node instanceof ReflectiveTouchable) {
				return fmap(Maybe.Util.<Touchable>just(), ((ReflectiveTouchable)node).shape());
			} else
				return Expressions.constant(Maybe.Util.<Touchable>nothing());
		}
	};
	
	TouchableProvider<Node> DEFAULT = Util.applyTransformAndAddVisualGroupsAndConnections(LOCAL_REFLECTIVE);
	
	public final class Util{
		public static <N> Expression<Function<N, Maybe<? extends Touchable>>> asAWhole(final TouchableProvider<N> tp) {
			return joinFunction(tp);
		}
		
		
		static Expression<? extends Maybe<? extends Touchable>> just(Expression<? extends Touchable> result) {
			return fmap(Maybe.Util.<Touchable>just(), result);
		}
		
		public static <N> Function<N, Expression<Touchable>> podgonHideMaybe(final TouchableProvider<N> tp) {
			return new Function<N, Expression<Touchable>>(){
				@Override
				public Expression<Touchable> apply(N argument) {
					return fmap(new Function<Maybe<? extends Touchable>, Touchable>(){

						@Override
						public Touchable apply(Maybe<? extends Touchable> argument) {
							return Maybe.Util.orElse(argument, new Touchable(){
								@Override
								public boolean hitTest(Point2D.Double point) {
									System.out.println("podgon in TouchableProvider");
									return false;
								}

								@Override
								public Rectangle2D.Double getBoundingBox() {
									System.out.println("podgon in TouchableProvider");
									return null;
								}

								@Override
								public Point2D.Double getCenter() {
									System.out.println("podgon in TouchableProvider");
									return new Point2D.Double(0,0);
								}
							});
						}
					}, tp.apply(argument));
				}
				
			};
		}
		
		public static TouchableProvider<Node> applyTransformAndAddVisualGroupsAndConnections(final TouchableProvider<Node> localTP) {
			return new TouchableProvider<Node>() {
				@Override
				public Expression<? extends Maybe<? extends Touchable>> apply(Node node) {
					final Expression<? extends Maybe<? extends Touchable>> localTouchable;
					
					if(node instanceof VisualGroup)
						return just(VisualGroup.screenSpaceBounds(this, (VisualGroup)node));
					else
						if(node instanceof VisualConnection)
							throw new NotImplementedException();
							//return nothing();//just(VisualConnectionGui.getConnectionGui(podgonHideMaybe(this), (VisualConnection)node).shape());
						localTouchable = localTP.apply(node);
					
					final Expression<Point2D.Double> position = MovableController.TRANSFORM_PROVIDER.apply(node);
					
					final Function<Point2D.Double, Function<Touchable, Touchable>> translateFunc = curry(new Function2<Point2D.Double, Touchable, Touchable>() {
						@Override
						public Touchable apply(Point2D.Double translation, Touchable touchable) {
							return new TouchableTransformer(touchable, AffineTransform.getTranslateInstance(translation.getX(), translation.getY()));
						}
					});
					
					return Expressions.fmap(new Function2<Maybe<? extends Touchable>, Point2D.Double, Maybe<? extends Touchable>>(){
						@Override
						public Maybe<? extends Touchable> apply(Maybe<? extends Touchable> argument1, Point2D.Double argument2) {
							return Maybe.Util.applyFunc(argument1, translateFunc.apply(argument2));
						}
					}, localTouchable, position);
				}
			};
		}
		public static TouchableProvider<Node> empty() {
			return new TouchableProvider<Node>(){
				@Override
				public Expression<? extends Maybe<? extends Touchable>> apply(Node argument) {
					return Expressions.constant(Maybe.Util.<Touchable>nothing());
				}
			};
		}
	}
}
