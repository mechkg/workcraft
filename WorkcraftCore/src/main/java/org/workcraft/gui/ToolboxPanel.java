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

package org.workcraft.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.workcraft.annotations.Annotations;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorKeyListener;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.shared.CommonVisualSettings;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

@SuppressWarnings("serial")
public class ToolboxPanel extends JPanel implements GraphEditorKeyListener {
	
	
	class ToolTracker {
		ArrayList<GraphEditorTool> tools = new ArrayList<GraphEditorTool>();
		int nextIndex = 0;
		
		public void addTool(GraphEditorTool tool) {
			tools.add(tool);
			nextIndex = 0;
		}
		
		public void reset() {
			nextIndex = 0;
		}
		
		public GraphEditorTool getNextTool() {
			GraphEditorTool ret = tools.get(nextIndex);
			setNext(nextIndex+1);
			return ret;
		}
		
		private void setNext(int next) {
			if(next >= tools.size())
				next %= tools.size();
			nextIndex = next;
		}

		public void track(GraphEditorTool tool) {
			setNext(tools.indexOf(tool)+1);
		}
	}

	private final ModifiableExpression<GraphEditorTool> selectedTool = Variable.<GraphEditorTool>create(null);

	private HashMap<GraphEditorTool, JToggleButton> buttons = new HashMap<GraphEditorTool, JToggleButton>();
	private HashMap<Integer, ToolTracker> hotkeyMap = new HashMap<Integer, ToolTracker>();

	public void addTool (final GraphEditorTool tool, boolean selected) {
		JToggleButton button = new JToggleButton();

		button.setFocusable(false);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setSelected(selected);
		button.setMargin(new Insets(0,0,0,0));
		
		Insets insets = button.getInsets();
		int iconSize = CommonVisualSettings.getIconSize();
		int minSize = iconSize+Math.max(insets.left+insets.right, insets.top+insets.bottom);
		
		Icon icon = tool.getIcon();
		if(icon==null) {
			button.setText(tool.getLabel());
			button.setPreferredSize(new Dimension(120,minSize));
		}
		else {
			BufferedImage crop = new BufferedImage(iconSize, iconSize,
					BufferedImage.TYPE_INT_ARGB);
			icon.paintIcon(button, crop.getGraphics(), (iconSize-icon.getIconWidth())/2, (iconSize-icon.getIconHeight())/2);
			button.setIcon(new ImageIcon(crop));
			button.setPreferredSize(new Dimension(minSize,minSize));
		}
	
		
		int hotKeyCode = tool.getHotKeyCode(); 
		if ( hotKeyCode != -1)
			button.setToolTipText("["+Character.toString((char)hotKeyCode)+"] " + tool.getLabel());
		else
			button.setToolTipText(tool.getLabel());

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectTool(tool);
			}
		});

		buttons.put(tool, button);
		
		if (hotKeyCode != -1) {
			ToolTracker tracker = hotkeyMap.get(hotKeyCode);
			if (tracker == null) {
				tracker = new ToolTracker();
				hotkeyMap.put(hotKeyCode, tracker);
			}
			tracker.addTool(tool);
		}

		this.add(button);
		
		if (selected)
			selectTool(tool);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends GraphEditorTool> T getToolInstance (Class<T> cls)
	{
		for (GraphEditorTool tool : buttons.keySet())
			if (cls.isInstance(tool))
				return (T)tool;
		return null;
	}
	
	public void selectTool(GraphEditorTool tool) {
		if (eval(selectedTool) != null) {
			ToolTracker oldTracker = hotkeyMap.get(eval(selectedTool).getHotKeyCode());
			if(oldTracker!=null)
				oldTracker.reset();
			
			eval(selectedTool).deactivated();
			buttons.get(eval(selectedTool)).setSelected(false);
		}

		ToolTracker tracker = hotkeyMap.get(tool.getHotKeyCode());
		if (tracker != null)
			tracker.track(tool);
		
		tool.activated();
		controlPanel.setTool(tool);
		buttons.get(tool).setSelected(true);
		selectedTool.setValue(tool);
	}

	public void addCommonTools(GraphEditor editor) {
		if(true)throw new NotImplementedException();
		addTool(new SelectionTool(editor, null), true);
		addTool(new ConnectionTool(editor, null), false);
	}

	private void setToolsForModel (VisualModelDescriptor modelDescriptor, GraphEditor editor) {
		setLayout(new SimpleFlowLayout (5, 5));
		
		try{
			Iterable<? extends GraphEditorTool> tools = modelDescriptor.createTools(editor);
			for(GraphEditorTool tool : tools)
				addTool(tool, false);
			selectTool(tools.iterator().next());
			return;
		}
		catch(NotImplementedException e) {
		}
		
		Class<? extends CustomToolsProvider> customTools = Annotations.getCustomToolsProvider(editor.getModel().getClass());
		if(customTools != null)
		{
			boolean selected = true;
			CustomToolsProvider provider = null;
			try {
				provider = customTools.getConstructor().newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(provider != null)
				for(GraphEditorTool tool : provider.getTools(editor))
				{
					addTool(tool, selected);
					selected = false;
				}
		}
		else
		{
			addCommonTools(editor);
		}
		
		for (Class<? extends GraphEditorTool>  tool : Annotations.getCustomTools(editor.getModel().getClass()))
			try {
				addTool(tool.newInstance() , false);
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		
		doLayout();
		this.repaint();
	}

	public ToolboxPanel(GraphEditorPanel editor, VisualModelDescriptor descriptor) {
		this.setFocusable(false);

		selectedTool.setValue(null);

		setToolsForModel(descriptor, editor);
	}

	public Expression<GraphEditorTool> selectedTool() {
		return selectedTool;
	}

	public void keyPressed(GraphEditorKeyEvent event) {
		if (!event.isAltDown() && !event.isCtrlDown() && !event.isShiftDown()) {
			int keyCode = event.getKeyCode();
			ToolTracker tracker = hotkeyMap.get(keyCode);
			if (tracker != null)
				selectTool(tracker.getNextTool());
			else
				eval(selectedTool).keyPressed(event);
		} else
			eval(selectedTool).keyPressed(event);
	}

	public void keyReleased(GraphEditorKeyEvent event) {
		eval(selectedTool).keyReleased(event);
		
	}

	public void keyTyped(GraphEditorKeyEvent event) {
		eval(selectedTool).keyTyped(event);
	}

	ToolInterfaceWindow controlPanel = new ToolInterfaceWindow();
	
	public ToolInterfaceWindow getControlPanel() {
		return controlPanel;
	}
}