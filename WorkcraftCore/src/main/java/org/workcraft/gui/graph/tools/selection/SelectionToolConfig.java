package org.workcraft.gui.graph.tools.selection;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.gui.graph.tools.HitTester;
import org.workcraft.gui.graph.tools.MovableController;
import org.workcraft.util.Function;

import pcollections.PSet;

public interface SelectionToolConfig<N> {
	ModifiableExpression<PSet<Node>> selection();
	HitTester<? extends N> hitTester();
	MovableController<N> movableController();
	Function<Point2D, Point2D> snap();
	Expression<? extends Node> currentEditingLevel();
	
	public class Default implements SelectionToolConfig<org.workcraft.dom.Node> {
		private final Function<Point2D, Point2D> snap;
		private final HitTester<? extends Node> hitTester;
		private final ModifiableExpression<PSet<Node>> selection;
		private final ModifiableExpression<VisualGroup> currentLevel;

		public Default(HitTester<? extends Node> hitTester, Function<Point2D, Point2D> snap, ModifiableExpression<PSet<Node>> selection, ModifiableExpression<VisualGroup> currentLevel) {
			this.hitTester = hitTester;
			this.snap = snap;
			this.currentLevel = currentLevel;
			this.selection = selection;
		}
		
		@Override
		public ModifiableExpression<PSet<Node>> selection() {
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
		public Expression<? extends Node> currentEditingLevel() {
			return currentLevel;
		}

		@Override
		public Function<Point2D, Point2D> snap() {
			return snap;
		}
	}
}
