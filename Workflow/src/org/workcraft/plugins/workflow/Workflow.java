package org.workcraft.plugins.workflow;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathGroup;

@VisualClass("org.workcraft.plugins.workflow.VisualWorkflow")
public class Workflow extends AbstractMathModel {

	public Workflow(Container root) {
		super(root);
	}

	public Workflow() {
		this(new MathGroup());
	}
	
}
