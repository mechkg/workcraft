package org.workcraft.gui.graph.tools.selection;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.Node;
import org.workcraft.gui.graph.tools.HitTester;
import org.workcraft.gui.graph.tools.MovableController;
import org.workcraft.util.Function;

import pcollections.HashTreePSet;
import pcollections.PSet;

public interface SelectionToolConfig<N> {
	ModifiableExpression<PSet<N>> selection();
	HitTester<? extends N> hitTester();
	MovableController<N> movableController();
	Function<Point2D, Point2D> snap();
	Expression<? extends Node> currentEditingLevel();
	
	public class Default implements SelectionToolConfig<org.workcraft.dom.Node> {
		private final Function<Point2D, Point2D> snap;
		private final HitTester<? extends Node> hitTester;

		public Default(HitTester<? extends Node> hitTester, Function<Point2D, Point2D> snap) {
			this.hitTester = hitTester;
			this.snap = snap;
		}
		
		Variable<PSet<Node>> selection = Variable.<PSet<Node>>create(HashTreePSet.<Node>empty());
		
		@Override
		public ModifiableExpression<PSet<Node>> selection() {
			// TODO: fill from proper editor-state
			return selection;
		}

		@Override
		public HitTester<? extends Node> hitTester() {
			return hitTester;
		}

		@Override
		public MovableController<Node> movableController() {
			return MovableController.REFLECTIVE_HIERARCHICAL;
		}

		@Override
		public Expression<Node> currentEditingLevel() {
			// TODO: fill from proper editor-state
			return Expressions.constant(null);
		}

		@Override
		public Function<Point2D, Point2D> snap() {
			return snap;
		}
	}
}
