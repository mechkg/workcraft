package org.workcraft.plugins.stg;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceManager;

public interface StgTextRefMan extends ReferenceManager {
	public void setName(Node node, String newName);
}
