package org.workcraft.dom.visual;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Node;
import org.workcraft.gui.graph.tools.MovableController;
import org.workcraft.util.Function2;
import org.workcraft.util.Graphics;
import org.workcraft.util.Maybe;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;

interface VisualSceneNode {
	Maybe<ModifiableExpression<Point2D>> movable();
	GraphicalContent gc();
	Maybe<Touchable> touchable();
}

class Scene {
	
}


public interface VisualScene<N> {
	public TouchableProvider<N> tp();
	public MovableController<N> mv();
	public Expression<? extends GraphicalContent> gc();
	
	public static Function2<VisualScene<Node>, VisualScene<Node>, VisualScene<Node>> combine = new Function2<VisualScene<Node>, VisualScene<Node>, VisualScene<Node>>(){
		@Override
		public VisualScene<Node> apply(final VisualScene<Node> scene1, final VisualScene<Node> scene2) {
			return Util.combine(scene1, scene2);
		}
	};

	public class Util {
		public static VisualScene<Node> combine(final VisualScene<Node> scene1, final VisualScene<Node> scene2) {
			return new VisualScene<Node>() {
				@Override
				public TouchableProvider<Node> tp() {
					return new TouchableProvider<Node>(){
						@Override
						public Expression<? extends Maybe<? extends Touchable>> apply(Node argument) {
							return fmap(Maybe.Util.<Touchable>first(), scene1.tp().apply(argument), scene2.tp().apply(argument));
						}
					};
				}

				@Override
				public MovableController<Node> mv() {
					return new MovableController<Node>() {
						@Override
						public Maybe<ModifiableExpression<Point2D.Double>> apply(Node node) {
							return Maybe.Util.first(scene1.mv().apply(node), scene2.mv().apply(node));
						}
					};
				}

				@Override
				public Expression<? extends GraphicalContent> gc() {
					return Graphics.compose(scene1.gc(), scene2.gc());
				}
			};
		}
		public static VisualScene<Node> empty(){
			return new VisualScene<Node>(){

				@Override
				public TouchableProvider<Node> tp() {
					return TouchableProvider.Util.empty();
				}

				@Override
				public MovableController<Node> mv() {
					return MovableController.Util.empty();
				}

				@Override
				public Expression<? extends GraphicalContent> gc() {
					return constant(GraphicalContent.EMPTY);
				}
			};
		}
	}
}
