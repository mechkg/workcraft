package org.workcraft.plugins.cpog;

import java.awt.event.KeyEvent;
import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.observation.PropertyChangedEvent;

public class SelectionTool extends org.workcraft.gui.graph.tools.SelectionTool
{
	@Override
	public void mouseClicked(GraphEditorMouseEvent e)
	{
		super.mouseClicked(e);
		if (e.getClickCount() > 1)
		{
			Collection<Node> selection = e.getModel().getSelection();
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
						Encoding encoding = scenario.getEncoding();
						encoding.toggleState(var);
						scenario.sendNotification(new PropertyChangedEvent(scenario, "encoding"));
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