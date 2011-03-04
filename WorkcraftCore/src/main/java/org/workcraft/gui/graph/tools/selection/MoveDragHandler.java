package org.workcraft.gui.graph.tools.selection;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Setter;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.gui.graph.tools.DragHandle;
import org.workcraft.gui.graph.tools.DragHandler;
import org.workcraft.gui.graph.tools.MovableController;
import org.workcraft.util.Function;
import org.workcraft.util.Geometry;

import pcollections.PCollection;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class MoveDragHandler<Node> implements DragHandler<Node> {
	private final Function<Point2D, Point2D> snap;
	private final MovableController<Node> movableController;
	private final Expression<? extends PCollection<? extends Node>> selection;
	
	public MoveDragHandler(Expression<? extends PCollection<? extends Node>> selection, MovableController<Node> movableController, Function<Point2D, Point2D> snap) {
		this.selection = selection;
		this.snap = snap;
		this.movableController = movableController;
	}

	private void offsetSelection (Point2D offset) {
		for(Node node : GlobalCache.eval(selection)) {
			ModifiableExpression<Point2D> pos = movableController.position(node);
			if(pos != null) {
				pos.setValue(Geometry.add(eval(pos), offset));
			}
		}
	}
	
	@Override
	public DragHandle startDrag(Node hitNode) {
		assert (hitNode != null);
		final ModifiableExpression<Point2D> nodePos = nodePos(hitNode, movableController);

		final Point2D originalPosition = eval(nodePos);
		
		final Setter<Point2D> snapper = new Setter<Point2D>() {
			@Override
			public void setValue(Point2D newValue) {
				Point2D current = eval(nodePos);
				Point2D newSnapped = snap.apply(newValue);
				Point2D offset = Geometry.subtract(newSnapped, current);
				offsetSelection(offset);
			}
		};
		
		snapper.setValue(originalPosition);

		return new DragHandle() {
			@Override
			public void setOffset(Point2D offset) {
				snapper.setValue(Geometry.add(originalPosition, offset));
			}
			
			@Override
			public void commit() {
			}
			
			@Override
			public void cancel() {
				nodePos.setValue(originalPosition);
			}
		};
	}

	private static <Node> ModifiableExpression<Point2D> nodePos(Node hitNode, MovableController<Node> movableController) {
		ModifiableExpression<Point2D> tmp = movableController.position(hitNode);
		final ModifiableExpression<Point2D> nodePos = tmp != null ? tmp : Variable.<Point2D>create(new Point2D.Double(0,0)); // dummy position to avoid a special case
		return nodePos;
	}
}
