package org.workcraft.gui.graph.tools.selection;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.assign;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.tools.DragHandle;
import org.workcraft.gui.graph.tools.HitTester;
import org.workcraft.gui.graph.tools.selection.GenericSelectionTool.SelectionMode;
import org.workcraft.util.Geometry;

import pcollections.HashTreePSet;
import pcollections.PSet;

public final class SelectionDragHandler<Node> {
	public SelectionDragHandler(ModifiableExpression<PSet<Node>> selection, HitTester<? extends Node> hitTester) {
		this.selection = selection;
		this.hitTester = hitTester;
	}
	
	protected final Color selectionBorderColor = new Color(200, 200, 200);
	protected final Color selectionFillColor = new Color(99, 130, 191, 32);

	
	/**
	 * Differs from a simple rectangle in that the order of corners does matter.
	 */
	class SelectionRectangle {
		public SelectionRectangle(Point2D.Double p1, Point2D.Double p2) {
			this.p1 = p1;
			this.p2 = p2;
		}
		public final Point2D.Double p1;
		public final Point2D.Double p2;
		Rectangle2D asRectangle(){
			return new Rectangle2D.Double(
					Math.min(p1.getX(), p2.getX()),
					Math.min(p1.getY(), p2.getY()),
					Math.abs(p1.getX()-p2.getX()),
					Math.abs(p1.getY()-p2.getY())
			);
		}
	}
	
	
	private final Variable<SelectionRectangle> selectionBox = Variable.create(null);
	public final Expression<PSet<Node>> effectiveSelection = new ExpressionBase<PSet<Node>>(){

		@Override
		protected PSet<Node> evaluate(EvaluationContext context) {
			PSet<Node> sel = context.resolve(selection);
			PSet<Node> delta = context.resolve(selectionBoxContents);
			switch(context.resolve(selectionMode)) {
			case ADD:
				return sel.plusAll(delta);
			case REMOVE:
				return sel.minusAll(delta);
			case NONE:
				return sel;
			case REPLACE:
				return delta;
			default:
				throw new NotSupportedException("selection mode " + selectionMode + " is not supported");
			}
		}
	};
	
	private final Variable<SelectionMode> selectionMode = Variable.create(SelectionMode.NONE);
	private final ModifiableExpression<PSet<Node>> selection;
	private final HitTester<? extends Node> hitTester;

	private final Expression<PSet<Node>> selectionBoxContents = new ExpressionBase<PSet<Node>>(){
		@Override
		protected PSet<Node> evaluate(EvaluationContext context) {
			SelectionRectangle selBox = context.resolve(selectionBox);
			return selBox == null ?
					HashTreePSet.<Node>empty() :
					HashTreePSet.from(hitTester.boxHitTest(selBox.p1, selBox.p2));
		}		
	};
	
	public DragHandle startDrag(final Point2D.Double dragStart, final SelectionMode mode) {
		selectionMode.setValue(mode);
		return new DragHandle() {
			@Override
			public void setOffset(Point2D.Double offset) {
				selectionBox.setValue(new SelectionRectangle(dragStart, Geometry.add(dragStart, offset)));
			}
			
			@Override
			public void commit() {
				assign(selection, effectiveSelection);
				selectionBox.setValue(null);
				selectionMode.setValue(SelectionMode.NONE);
			}
			
			@Override
			public void cancel() {
				selectionBox.setValue(null);
			}
		};
	}
	
	public Expression<GraphicalContent> graphicalContent(final Viewport viewPort) {
		return new ExpressionBase<GraphicalContent>() {

			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent(){

					@Override
					public void draw(Graphics2D g) {
						if(context.resolve(selectionBox)!=null) {
							g.setStroke(new BasicStroke((float) viewPort.pixelSizeInUserSpace().getX()));
							
							g.setColor(selectionFillColor);
							g.fill(context.resolve(selectionBox).asRectangle());
							g.setColor(selectionBorderColor);
							g.draw(context.resolve(selectionBox).asRectangle());
						}
					}
				};
			}
		};
	}	
}
