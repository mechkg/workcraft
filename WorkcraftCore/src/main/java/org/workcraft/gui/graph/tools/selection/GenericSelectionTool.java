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

package org.workcraft.gui.graph.tools.selection;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.tools.DragHandle;
import org.workcraft.gui.graph.tools.DragHandler;
import org.workcraft.gui.graph.tools.HitTester;
import org.workcraft.util.Geometry;

import pcollections.HashTreePSet;
import pcollections.PSet;

public class GenericSelectionTool<Node> {
	
	private final HitTester<? extends Node> hitTester;
	enum SelectionMode {
		NONE,
		ADD,
		REMOVE,
		REPLACE
	}

	private boolean notClick1 = false;
	private boolean notClick3 = false;
	
	private static DragHandle noDrag = new DragHandle() {
		@Override
		public void setOffset(Point2D offset) {
		}
		
		@Override
		public void commit() {
		}
		
		@Override
		public void cancel() {
		}
	};
	
	private DragHandle currentDrag = noDrag;
	private final DragHandler<Node> nodeDragHandler;
	private final SelectionDragHandler<Node> selectDragHandler;
	
	public GenericSelectionTool(
			ModifiableExpression<PSet<Node>> selection, 
			HitTester<? extends Node> hitTester, 
			DragHandler<Node> nodeDragHandler) {
		this.selection = selection;
		this.hitTester = hitTester;
		this.nodeDragHandler = nodeDragHandler;
		this.selectDragHandler = new SelectionDragHandler<Node>(selection, hitTester);
	}
	
	public boolean isDragging() {
		return currentDrag!=noDrag;
	}
	
	public final ModifiableExpression<PSet<Node>> selection;
	
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
	
	public void mouseMoved(GraphEditorMouseEvent e) {
		currentDrag.setOffset(Geometry.subtract(e.getPosition(), e.getStartPosition()));
	}

	public void startDrag(GraphEditorMouseEvent e) {

		assert(!isDragging());
		if(e.getButtonModifiers()==MouseEvent.BUTTON1_DOWN_MASK) {
			Node hitNode = hitTester.hitTest(e.getStartPosition());

			if (hitNode == null) {
				// hit nothing, so start select-drag
				
				SelectionMode mode;
				
				switch(e.getKeyModifiers()) {
					case 0:
						mode = SelectionMode.REPLACE;
						break;
					case MouseEvent.CTRL_DOWN_MASK:
						mode = SelectionMode.REMOVE;
						break;
					case MouseEvent.SHIFT_DOWN_MASK:
						mode = SelectionMode.ADD;
						break;
					default:
						mode = SelectionMode.NONE;
				}
				
				if(mode!=SelectionMode.NONE) {
					// selection will not actually be changed until drag completes
					currentDrag = selectDragHandler.startDrag(e.getStartPosition(), mode);
				}

			} else {
				// hit something
				if(e.getKeyModifiers()==0) {
					// mouse down without modifiers, begin move-drag
					if(hitNode!=null && !eval(selection).contains(hitNode))
						selection.setValue(HashTreePSet.singleton(hitNode));

					currentDrag = nodeDragHandler.startDrag(hitNode);
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
		currentDrag.commit();
		currentDrag = noDrag;
	}
	
	private void cancelDrag(GraphEditorMouseEvent e) {
		currentDrag.cancel();
		currentDrag = noDrag;
	}

	public Expression<GraphicalContent> userSpaceContent(final Viewport viewPort) {
		return selectDragHandler.graphicalContent(viewPort);
	}

	public Expression<PSet<Node>> effectiveSelection() {
		return selectDragHandler.effectiveSelection;
	}
}
