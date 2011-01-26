package org.workcraft.plugins.workflow;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Container;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathGroup;

public class Workflow extends AbstractMathModel {

	public Workflow(Container root, StorageManager storage) {
		super(root);
	}

	public Workflow(StorageManager storage) {
		this(new MathGroup(storage), storage);
	}
	
}
