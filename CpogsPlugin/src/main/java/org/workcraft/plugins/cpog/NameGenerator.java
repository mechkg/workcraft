package org.workcraft.plugins.cpog;


public class NameGenerator {

	private int vertexCount = 0;
	private int variableCount = 0;
	
	public void assignDefaultLabel(VisualVertex vertex) {
		vertex.label().setValue("v_" + vertexCount++);
	}
	
	public void assignDefaultLabel(VisualVariable variable) {
		variable.label().setValue("x_" + variableCount++);
	}
}
