package org.workcraft.gui.graph.tools;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.util.Function;

import pcollections.HashTreePSet;
import pcollections.PSet;

public interface SelectionToolConfig<N> {
	ModifiableExpression<PSet<N>> selection();
	HitTester<N> hitTester();
	MovableController<N> movableController();
	Function<Point2D, Point2D> snap();
	
	public class Default implements SelectionToolConfig<org.workcraft.dom.Node> {
		private final VisualModel model;
		private final TouchableProvider<Node> tp;
		private final Function<Point2D, Point2D> snap;

		public Default(VisualModel model, TouchableProvider<Node> tp, Function<Point2D, Point2D> snap) {
			this.model = model;
			this.tp = tp;
			this.snap = snap;
		}
		
		@Override
		public ModifiableExpression<PSet<Node>> selection() {
			// TODO: fill from proper editor-state
			return Variable.<PSet<Node>>create(HashTreePSet.<Node>empty());
		}

		@Override
		public HitTester<Node> hitTester() {
			return new HitTester<Node>() {
				@Override
				public Node hitTest(Point2D point) {
					return HitMan.hitTestForSelection(tp, point, model);
				}

				private Point2D transformToCurrentSpace(Point2D pointInRootSpace)
				{
					Point2D newPoint = new Point2D.Double();
					TransformHelper.getTransform(model.getRoot(), eval(model.currentLevel())).transform(pointInRootSpace, newPoint);
					return newPoint;
				}
				
				@Override
				public PSet<Node> boxHitTest(Point2D boxStart, Point2D boxEnd) {
					boxStart = transformToCurrentSpace(boxStart); // TODO: find out why current
					boxEnd = transformToCurrentSpace(boxEnd);
					return HashTreePSet.from(HitMan.boxHitTest(tp, eval(model.currentLevel()), boxStart, boxEnd));
				}
			};
		}

		@Override
		public MovableController<Node> movableController() {
			return MovableController.REFLECTIVE_HIERARCHICAL;
		}

		@Override
		public ModifiableExpression<Node> currentEditingLevel() {
			return Expressions.cast(model.currentLevel(), Container.class, Node.class);
		}

		@Override
		public Function<Point2D, Point2D> snap() {
			return snap;
		}
	}

	Expression<Node> currentEditingLevel();
}
