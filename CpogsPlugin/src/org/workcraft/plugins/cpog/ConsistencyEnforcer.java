package org.workcraft.plugins.cpog;

import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchySupervisor;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class ConsistencyEnforcer extends HierarchySupervisor {

	private final VisualCPOG visualCPOG;
	private int vertexCount = 0;
	private int variableCount = 0;
	
	public ConsistencyEnforcer(VisualCPOG visualCPOG)
	{
		super(visualCPOG.getRoot());
		this.visualCPOG  = visualCPOG;
		start();
	}
	
	@Override
	public void handleEvent(List<Node> added, List<Node> removed) 
	{
		updateEncoding();
		createDefaultLabels(added);
	}
	
	private void createDefaultLabels(List<Node> added) {
		for(Node node : added)
		{
			if (node instanceof VisualVertex)
			{
				((VisualVertex) node).setLabel("v_" + vertexCount++);
			}
			if (node instanceof VisualVariable)
			{
				((VisualVariable) node).setLabel("x_" + variableCount++);
			}
		}
	}

	private void updateEncoding()
	{
		for(VisualScenario group : visualCPOG.getGroups())
		{
			Encoding oldEncoding = eval(group.encoding());
			Encoding newEncoding = new Encoding();
		
			for(VisualVariable var : visualCPOG.getVariables())
			{
				Variable mathVariable = var.getMathVariable();
				newEncoding.setState(mathVariable, oldEncoding.getState(mathVariable));
			}
		
			group.encoding().setValue(newEncoding);
		}
		
	}

}
