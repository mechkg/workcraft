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

package org.workcraft.gui.events;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.workcraft.gui.graph.tools.GraphEditor;

public class GraphEditorMouseEvent {
	final MouseEvent event;
	final Point2D.Double position;
	final Point2D.Double prevPosition;
	final Point2D.Double startPosition;

	public GraphEditorMouseEvent(GraphEditor editor, MouseEvent e, Point2D.Double startPosition, Point2D.Double prevPosition) {
		event = e;
		position = editor.getViewport().screenToUser(e.getPoint());
		this.startPosition = startPosition == null ? position : startPosition;
		this.prevPosition = prevPosition;
	}
	
	public MouseEvent getSystemEvent() {
		return event;
	}
	
	public int getID() {
		return event.getID();
	}

	public Point2D.Double getPosition() {
		return position;
	}

	public Point2D.Double getStartPosition() {
		return startPosition;
	}

	public Point2D.Double getPrevPosition() {
		return prevPosition;
	}

	public double getX() {
		return position.getX();
	}

	public double getY() {
		return position.getY();
	}

	public int getButton() {
		return event.getButton();
	}

	public int getClickCount() {
		return event.getClickCount();
	}

	public int getModifiers() {
		return event.getModifiersEx();
	}
	
	public int getKeyModifiers() {
		return event.getModifiersEx() & (MouseEvent.SHIFT_DOWN_MASK|MouseEvent.CTRL_DOWN_MASK|MouseEvent.ALT_DOWN_MASK);
	}

	public int getButtonModifiers() {
		// BUTTON2 is ignored as it is reserved for panning
		return event.getModifiersEx() & (MouseEvent.BUTTON1_DOWN_MASK|MouseEvent.BUTTON3_DOWN_MASK);
	}
}
