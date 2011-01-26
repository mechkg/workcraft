package org.workcraft.dom;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.math.MathModel;


public interface ModelDescriptor {
	String getDisplayName();
	MathModel createMathModel(StorageManager storage);
	VisualModelDescriptor getVisualModelDescriptor(); 
}
