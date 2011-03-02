/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui.graph.tools;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.assign;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.util.Function;

import pcollections.HashTreePSet;
import pcollections.PSet;

public class GenericSelectionTool<Node> {
	
	public interface DragHandler {
		void drag(double dx, double dy);
	}
	
	private final HitTester<Node> hitTester;
	private final MovableController<Node> movableController;
	
	private static final int DRAG_NONE = 0;
	private static final int DRAG_MOVE = 1;
	private static final int DRAG_SELECT = 2;

	enum SelectionMode {
		NONE,
		ADD,
		REMOVE,
		REPLACE
	}

	protected Color selectionBorderColor = new Color(200, 200, 200);
	protected Color selectionFillColor = new Color(99, 130, 191, 32);

	private ModifiableExpression<Integer> drag = Variable.create(DRAG_NONE);
	private boolean notClick1 = false;
	private boolean notClick3 = false;
	
	private Point2D snapOffset;
	
	private Variable<SelectionMode> selectionMode = Variable.create(SelectionMode.NONE);
	
	class SelectionRectangle {
		public SelectionRectangle(Point2D p1, Point2D p2) {
			this.p1 = p1;
			this.p2 = p2;
		}
		public final Point2D p1;
		public final Point2D p2;
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

	private final Expression<PSet<Node>> selectionBoxContents = new ExpressionBase<PSet<Node>>(){
		@Override
		protected PSet<Node> evaluate(EvaluationContext context) {
			SelectionRectangle selBox = context.resolve(selectionBox);
			return selBox == null ?
					HashTreePSet.<Node>empty() :
					hitTester.boxHitTest(selBox.p1, selBox.p2);
		}		
	};
	private final Function<Point2D, Point2D> snap;

	public GenericSelectionTool(ModifiableExpression<PSet<Node>> selection, HitTester<Node> hitTester, MovableController<Node> movableController, Function<Point2D, Point2D> snap) {
		this.selection = selection;
		this.hitTester = hitTester;
		this.movableController = movableController;
		this.snap = snap;
	}
	
	public boolean isDragging() {
		return eval(drag)!=DRAG_NONE;
	}
	
	public final ModifiableExpression<PSet<Node>> selection;
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
	
	public void mouseClicked(GraphEditorMouseEvent e) {

		if(notClick1 && e.getButton() == MouseEvent.BUTTON1)
			return;
		if(notClick3 && e.getButton() == MouseEvent.BUTTON3)
			return;
		
		if(e.getButton()==MouseEvent.BUTTON1) {
			Node node = hitTester.hitTest(e.getPosition());
			if (node != null)
			{
				switch(e.getKeyModifiers()) {
					case 0:
						selection.setValue(HashTreePSet.singleton(node));
						break;
					case MouseEvent.SHIFT_DOWN_MASK:
						selection.setValue(eval(selection).plus(node));
						break;
					case MouseEvent.CTRL_DOWN_MASK:
						selection.setValue(eval(selection).minus(node));
						break;
				}
			} else {
				if (e.getKeyModifiers()==0)
					selection.setValue(HashTreePSet.<Node>empty());
			}
		}
	}

	private void offsetSelection (double dx, double dy) {
		for(Node node : GlobalCache.eval(selection)) {
			ModifiableExpression<Point2D> pos = movableController.position(node);
			if(pos != null) {
				pos.setValue(new Point2D.Double(dx, dy));
			}
		}
	}
	
	public void mouseMoved(GraphEditorMouseEvent e) {
		
		if(eval(drag)==DRAG_MOVE) {
			Point2D prevPosition = e.getPrevPosition();
			Point2D p1 = snap.apply(
					new Point2D.Double(prevPosition.getX()+
							snapOffset.getX(), 
							prevPosition.getY()+
							snapOffset.getY()));
			Point2D p2 = snap.apply(new Point2D.Double(e.getX()+snapOffset.getX(), e.getY()+snapOffset.getY()));
			offsetSelection(p2.getX()-p1.getX(), p2.getY()-p1.getY());
		}
		
		else if(eval(drag)==DRAG_SELECT) {
			selectionBox.setValue(new SelectionRectangle(e.getStartPosition(), e.getPosition()));
		}
	}

	public void startDrag(GraphEditorMouseEvent e) {

		if(e.getButtonModifiers()==MouseEvent.BUTTON1_DOWN_MASK) {
			Node hitNode = hitTester.hitTest(e.getStartPosition());

			if (hitNode == null) {
				// hit nothing, so start select-drag
				
				switch(e.getKeyModifiers()) {
					case 0:
						selectionMode.setValue(SelectionMode.REPLACE);
						break;
					case MouseEvent.CTRL_DOWN_MASK:
						selectionMode.setValue(SelectionMode.REMOVE);
						break;
					case MouseEvent.SHIFT_DOWN_MASK:
						selectionMode.setValue(SelectionMode.ADD);
						break;
					default:
						selectionMode.setValue(SelectionMode.NONE);
				}
				
				if(eval(selectionMode)!=SelectionMode.NONE) {
					// selection will not actually be changed until drag completes
					drag.setValue(DRAG_SELECT);
					
					if(eval(selectionMode)==SelectionMode.REPLACE)
						selection.setValue(HashTreePSet.<Node>empty());
				}

			} else {
				// hit something
				ModifiableExpression<Point2D> nodePos = movableController.position(hitNode);
				if(nodePos != null && e.getKeyModifiers()==0) {
					// mouse down without modifiers, begin move-drag
					drag.setValue(DRAG_MOVE);
					
					if(hitNode!=null && !eval(selection).contains(hitNode))
						selection.setValue(HashTreePSet.singleton(hitNode));

					Point2D nodePosValue = eval(nodePos);
					Point2D pos = new Point2D.Double(nodePosValue.getX(), nodePosValue.getY());
					Point2D pSnap = snap.apply(pos);
					offsetSelection(pSnap.getX()-pos.getX(), pSnap.getY()-pos.getY());
					snapOffset = new Point2D.Double(pSnap.getX()-e.getStartPosition().getX(), pSnap.getY()-e.getStartPosition().getY());
				}
				// do nothing if pressed on a node with modifiers
				
			}
		}
	}

	public void mousePressed(GraphEditorMouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON1)
			notClick1 = false;
		
		if(e.getButton()==MouseEvent.BUTTON3) {
			
			if(isDragging()) {
				cancelDrag(e);
				notClick1 = true;
				notClick3 = true;
			}
			else {
				notClick3 = false;
			}
		}
	}
	
	public void finishDrag(GraphEditorMouseEvent e) {
		if (eval(drag) == DRAG_SELECT)
		{
			assign(selection, effectiveSelection);
			selectionBox.setValue(null);
			selectionMode.setValue(SelectionMode.NONE);
		}
		drag.setValue(DRAG_NONE);
	}
	
	private void cancelDrag(GraphEditorMouseEvent e) {

		if(eval(drag)==DRAG_MOVE) {
			Point2D p1 = snap.apply(new Point2D.Double(e.getStartPosition().getX()+snapOffset.getX(), e.getStartPosition().getY()+snapOffset.getY()));
			Point2D p2 = snap.apply(new Point2D.Double(e.getX()+snapOffset.getX(), e.getY()+snapOffset.getY()));
			offsetSelection(p1.getX()-p2.getX(), p1.getY()-p2.getY());
		}
		else if(eval(drag) == DRAG_SELECT) {
			selectionBox.setValue(null);
		}
		drag.setValue(DRAG_NONE);
	}

	public Expression<GraphicalContent> userSpaceContent(final Viewport viewPort) {
		return new ExpressionBase<GraphicalContent>() {

			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent(){

					@Override
					public void draw(Graphics2D g) {
						if(context.resolve(drag)==DRAG_SELECT && context.resolve(selectionBox)!=null) {
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
