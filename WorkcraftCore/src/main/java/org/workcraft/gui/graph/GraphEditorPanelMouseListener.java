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

package org.workcraft.gui.graph;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorMouseListener;

class GraphEditorPanelMouseListener implements MouseMotionListener, MouseListener, MouseWheelListener {
	protected GraphEditor editor;
	protected boolean panDrag = false;
	private Expression<GraphEditorMouseListener> toolMouseListener;

	protected Point lastMouseCoords = new Point();
	private Point2D.Double prevPosition = new Point2D.Double(0, 0);
	private Point2D.Double startPosition = null;
	
	public GraphEditorPanelMouseListener(GraphEditor editor, Expression<GraphEditorMouseListener> toolMouseListener) {
		this.editor = editor;
		this.toolMouseListener = toolMouseListener;
	}

	private GraphEditorMouseEvent adaptEvent(MouseEvent e) {
		return new GraphEditorMouseEvent(editor, e, startPosition, prevPosition);
	}
	
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}
	
	public void mouseMoved(MouseEvent e) {
		Point currentMouseCoords = e.getPoint();
		if (panDrag) {
			editor.getViewport().pan(currentMouseCoords.x - lastMouseCoords.x, currentMouseCoords.y - lastMouseCoords.y);
		} else {
			GraphEditorMouseListener toolMouseListener = eval(this.toolMouseListener);
			if(!toolMouseListener.isDragging() && startPosition!=null) {
				toolMouseListener.startDrag(adaptEvent(e));
			}
			toolMouseListener.mouseMoved(adaptEvent(e));
		}
		prevPosition = editor.getViewport().screenToUser(currentMouseCoords);
		lastMouseCoords = currentMouseCoords;
	}

	public void mouseClicked(MouseEvent e) {
		if (!editor.hasFocus())
			editor.getMainWindow().requestFocus((GraphEditorPanel)editor);
		 
		if (e.getButton() != MouseEvent.BUTTON2)
			eval(toolMouseListener).mouseClicked(adaptEvent(e));
	}

	public void mouseEntered(MouseEvent e) {
		if (editor.hasFocus()) {
			eval(toolMouseListener).mouseEntered(adaptEvent(e));
		}
	}

	public void mouseExited(MouseEvent e) {
		if (editor.hasFocus())
			eval(toolMouseListener).mouseExited(adaptEvent(e));
	}

	public void mousePressed(MouseEvent e) {
		if (!editor.hasFocus())
			editor.getMainWindow().requestFocus((GraphEditorPanel)editor);
		
		if (e.getButton() == MouseEvent.BUTTON2)
			panDrag = true;
		else {
			GraphEditorMouseListener toolML = eval(toolMouseListener);
			if(!toolML.isDragging())
				startPosition = editor.getViewport().screenToUser(e.getPoint());
			toolML.mousePressed(adaptEvent(e));
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2)
			panDrag = false;
		else {
			GraphEditorMouseListener toolML = eval(toolMouseListener);
			if(toolML.isDragging())
				toolML.finishDrag(adaptEvent(e));
			toolML.mouseReleased(adaptEvent(e));
			startPosition = null;
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (editor.hasFocus()) {
			editor.getViewport().zoom(-e.getWheelRotation(), e.getPoint());
		}
	}
}
