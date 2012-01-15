package org.workcraft.gui.graph.tools.selection;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;
import static org.workcraft.util.Maybe.Util.applyFunc;
import static org.workcraft.util.Maybe.Util.doIfJust;
import static org.workcraft.util.Maybe.Util.orElse;

import java.awt.geom.Point2D;
import java.util.HashMap;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Setter;
import org.workcraft.gui.graph.tools.DragHandle;
import org.workcraft.gui.graph.tools.DragHandler;
import org.workcraft.util.Action1;
import org.workcraft.util.Function;
import org.workcraft.util.Geometry;
import org.workcraft.util.Maybe;

import pcollections.PCollection;

public class MoveDragHandler<Node> implements DragHandler<Node> {
	private final Function<Point2D.Double, Point2D.Double> snap;
	private final Function<Node, Maybe<? extends ModifiableExpression<Point2D.Double>>> movableController;
	private final Expression<? extends PCollection<? extends Node>> selection;
	
	public MoveDragHandler(Expression<? extends PCollection<? extends Node>> selection, 
			Function<Node, Maybe<? extends ModifiableExpression<Point2D.Double>>> movableController, 
			Function<Point2D.Double, Point2D.Double> snap) {
		this.selection = selection;
		this.snap = snap;
		this.movableController = movableController;
	}
	
	@Override
	public DragHandle startDrag(final Node hitNode) {
		assert (hitNode != null);
		
		return new DragHandle() {

			final Point2D.Double originalPosition = getNodePos(hitNode, movableController);
			
			HashMap<Node, Point2D.Double> originalPositions = new HashMap<Node, Point2D.Double>();
			
			private Point2D.Double getOriginalPositionWithDefault(Node node, Point2D.Double pos) {
				final Point2D.Double res = originalPositions.get(node);
				if(res != null)
					return res;
				else {
					originalPositions.put(node, pos);
					return pos;
				}
			}
			
			private void offsetSelection (final Point2D.Double totalOffset) {
				for(final Node node : GlobalCache.eval(selection)) {
					doIfJust(movableController.apply(node), new Action1<ModifiableExpression<Point2D.Double>>(){
						@Override
						public void run(ModifiableExpression<Point2D.Double> pos) {
							Point2D.Double posVal = eval(pos);
							Point2D.Double origPosVal = getOriginalPositionWithDefault(node, posVal);
							pos.setValue(Geometry.add(origPosVal, totalOffset));
						}
					});
				}
			}
			
			final Setter<Point2D.Double> snapper = new Setter<Point2D.Double>() {
				@Override
				public void setValue(Point2D.Double newValue) {
					Point2D.Double newSnapped = snap.apply(newValue);
					Point2D.Double totalOffset = Geometry.subtract(newSnapped, originalPosition);
					offsetSelection(totalOffset);
				}
			};
			
			{
				snapper.setValue(originalPosition);
			}
			
			private Point2D.Double getNodePos(Node hitNode, Function<Node, Maybe<? extends ModifiableExpression<Point2D.Double>>> movableController) {
				return orElse(applyFunc(movableController.apply(hitNode), new Function<ModifiableExpression<Point2D.Double>, Point2D.Double>(){
					@Override
					public Point2D.Double apply(ModifiableExpression<Point2D.Double> argument) {
						return eval(argument);
					}
				}), new Point2D.Double(0, 0));
			}
			
			@Override
			public void setOffset(Point2D.Double offset) {
				snapper.setValue(Geometry.add(originalPosition, offset));
			}
			
			@Override
			public void commit() {
			}
			
			@Override
			public void cancel() {
				setOffset(new Point2D.Double(0,0));
			}
		};
	}
}
