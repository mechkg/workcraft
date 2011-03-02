package org.workcraft.dom;

import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;

public interface VisualModelDescriptor {
	public VisualModel create (MathModel mathModel, StorageManager storage) throws VisualModelInstantiationException;
}
