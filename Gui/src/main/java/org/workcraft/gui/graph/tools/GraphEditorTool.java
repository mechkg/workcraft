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

import javax.swing.Icon;
import javax.swing.JPanel;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.gui.graph.Viewport;

public interface GraphEditorTool {
	
	public interface Button {
		public String getLabel();
		public Icon getIcon();
		/**
		 * Returns the hot key code associated with this tool. They should be taken from the java.awt.event.KeyEvent class.
		 * -1 stands for "no hot-key". 
		 * @return
		 */
		public int getHotKeyCode();	
	}

	public GraphEditorKeyListener keyListener();
	public GraphEditorMouseListener mouseListener();
	
	public void activated();
	public void deactivated();
	
	public Expression<? extends GraphicalContent> userSpaceContent(Viewport viewport, Expression<Boolean> hasFocus);
	public Expression<? extends GraphicalContent> screenSpaceContent(Viewport viewport, Expression<Boolean> hasFocus);
	
	/**
	 * A panel with additional tool controls. Can be null.
	 */
	public JPanel getInterfacePanel();

	public Button getButton();
}
