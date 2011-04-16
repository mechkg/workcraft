package org.workcraft.dom.visual;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.util.Function.Util.*;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnectionGui;
import org.workcraft.gui.graph.tools.MovableController;
import org.workcraft.util.Function;
import org.workcraft.util.Function2;
import org.workcraft.util.Maybe;


public interface TouchableProvider<N> extends Function<N, Expression<? extends Maybe<? extends Touchable>>> {
	
	public final TouchableProvider<Node> LOCAL_REFLECTIVE = new TouchableProvider<Node> () {
		@Override
		public Expression<? extends Maybe<Touchable>> apply(Node node) {
			if (node instanceof ReflectiveTouchable) {
				return bindFunc(((ReflectiveTouchable)node).shape(), Maybe.Util.<Touchable>just());
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
			return bindFunc(result, Maybe.Util.<Touchable>just());
		}
		
		public static <N> Function<N, Expression<Touchable>> podgonHideMaybe(final TouchableProvider<N> tp) {
			return new Function<N, Expression<Touchable>>(){
				@Override
				public Expression<Touchable> apply(N argument) {
					return bindFunc(tp.apply(argument), new Function<Maybe<? extends Touchable>, Touchable>(){

						@Override
						public Touchable apply(Maybe<? extends Touchable> argument) {
							return Maybe.Util.orElse(argument, new Touchable(){
								@Override
								public boolean hitTest(Point2D point) {
									System.out.println("podgon in TouchableProvider");
									return false;
								}

								@Override
								public Rectangle2D getBoundingBox() {
									System.out.println("podgon in TouchableProvider");
									return null;
								}

								@Override
								public Point2D getCenter() {
									System.out.println("podgon in TouchableProvider");
									return new Point2D.Double(0,0);
								}
							});
						}
					});
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
							return just(VisualConnectionGui.getConnectionGui(podgonHideMaybe(this), (VisualConnection)node).shape());
						localTouchable = localTP.apply(node);
					
					final Expression<Point2D> position = MovableController.TRANSFORM_PROVIDER.apply(node);
					
					final Function<Point2D, Function<Touchable, Touchable>> translateFunc = curry(new Function2<Point2D, Touchable, Touchable>() {
						@Override
						public Touchable apply(Point2D translation, Touchable touchable) {
							return new TouchableTransformer(touchable, AffineTransform.getTranslateInstance(translation.getX(), translation.getY()));
						}
					});
					
					return Expressions.bindFunc(localTouchable, position, new Function2<Maybe<? extends Touchable>, Point2D, Maybe<? extends Touchable>>(){
						@Override
						public Maybe<? extends Touchable> apply(Maybe<? extends Touchable> argument1, Point2D argument2) {
							return Maybe.Util.applyFunc(argument1, translateFunc.apply(argument2));
						}
					});
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
