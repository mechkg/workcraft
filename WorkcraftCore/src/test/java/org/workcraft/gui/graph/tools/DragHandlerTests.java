package org.workcraft.gui.graph.tools;

import static org.junit.Assert.assertEquals;

import java.awt.geom.Point2D;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.gui.graph.tools.selection.MoveDragHandler;
import org.workcraft.util.Function;
import org.workcraft.util.Maybe;
import static org.workcraft.util.Maybe.Util.*;

import pcollections.PCollection;
import pcollections.TreePVector;

public class DragHandlerTests {
	private MoveDragHandler<Dummy> init(TreePVector<Dummy> selectionV) {
		Expression<? extends PCollection<? extends Dummy>> selection = Expressions.constant(selectionV);
		MovableController<Dummy> movableController = new MovableController<Dummy>(){
			@Override
			public Maybe<? extends ModifiableExpression<Point2D>> position(Dummy node) {
				return just(node.coordinate);
			}
		};
		Function<Point2D, Point2D> snap = new Function<Point2D, Point2D>() {

			@Override
			public Point2D apply(Point2D argument) {
				return argument;
			}
		};
		MoveDragHandler<Dummy> dragger = new MoveDragHandler<Dummy>(selection, movableController, snap);
		return dragger;
	}

	@Test
	public void testSimpleSingleNodeMove() {
		Dummy node = new Dummy();
		TreePVector<Dummy> selection = TreePVector.<Dummy>singleton(node);
		MoveDragHandler<Dummy> dragger = init(selection);
		DragHandle drag = dragger.startDrag(node);
		drag.setOffset(new Point2D.Double(1, 1));
		assertEquals(new Point2D.Double(1, 1), node.coordinate.getValue());
	}
	
	@Test
	public void testSimpleSingleNodeDoubleMove() {
		Dummy node = new Dummy();
		TreePVector<Dummy> selection = TreePVector.<Dummy>singleton(node);
		MoveDragHandler<Dummy> dragger = init(selection);
		DragHandle drag = dragger.startDrag(node);
		drag.setOffset(new Point2D.Double(1, 1));
		drag.setOffset(new Point2D.Double(2, 2));
		assertEquals(new Point2D.Double(2, 2), node.coordinate.getValue());
	}
	
}
