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

package org.workcraft.plugins.balsa;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.Icon;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.SelectionTool;

public class VisualBalsaTools implements CustomToolsProvider
{
	GraphEditorTool getComponentTool(final String componentName)
	{
		return new NodeGeneratorTool(new NodeGenerator(){
			
			@Override
			public String getLabel() {
				return componentName;
			}

			@Override
			public Icon getIcon() {
				return null;
			}

			@Override
			public void generate(VisualModel model, Point2D where)
					throws NodeCreationException {
				((VisualBalsaCircuit)model).createComponent(componentName, where);
			}

			@Override
			public int getHotKeyCode() {
				return -1;
			}
		});
	}
	
	@Override
	public ArrayList<GraphEditorTool> getTools(GraphEditor editor) {
		ArrayList<GraphEditorTool> tools = new ArrayList<GraphEditorTool>();
		
		//TODO
		@SuppressWarnings("unused")
		Class<?> [] balsaClasses = 
			new Class<?>[]
			{
			};
		
		tools.add(new SelectionTool(editor));
		tools.add(new ConnectionTool(editor));
		//for(Class<?> c : balsaClasses)
		//	tools.add(getComponentTool((Class<? extends org.workcraft.plugins.balsa.components.Component>) c));
		
		return tools;
	}
}
