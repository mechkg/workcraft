package org.workcraft.plugins.cpog;

import java.awt.event.KeyEvent;
import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.Node;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class SelectionTool extends org.workcraft.gui.graph.tools.SelectionTool
{
	@Override
	public void mouseClicked(GraphEditorMouseEvent e)
	{
		super.mouseClicked(e);
		if (e.getClickCount() > 1)
		{
			Collection<? extends Node> selection = GlobalCache.eval(e.getModel().selection());
			if(selection.size() == 1)
			{
				Node selectedNode = selection.iterator().next();
				
				if(selectedNode instanceof VisualVariable)
				{
					((VisualVariable) selectedNode).toggle();					
				}
				
				if(selectedNode instanceof VisualScenario)
				{
					VisualScenario scenario = (VisualScenario) selectedNode;
					Variable var = scenario.getVariableAt(e.getPosition());
					
					if (var == null) currentLevelDown(e.getModel());
					else
					{
						Encoding encoding = eval(scenario.encoding());
						scenario.encoding().setValue(encoding.toggleState(var));
					}
				}
			}
			
		}
	}
	@Override
	public void keyPressed(GraphEditorKeyEvent e)
	{
		super.keyPressed(e);	
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) currentLevelUp(e.getModel());
	}
}
