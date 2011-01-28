package org.workcraft.plugins.workflow;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Container;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathModel;

public class Workflow extends AbstractModel implements MathModel {

	public Workflow(Container root, StorageManager storage) {
		super(createDefaultModelSpecification(root));
	}

	public Workflow(StorageManager storage) {
		this(new MathGroup(storage), storage);
	}
	
}
