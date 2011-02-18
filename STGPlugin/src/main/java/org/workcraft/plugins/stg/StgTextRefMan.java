package org.workcraft.plugins.stg;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceManager;

/** 
 * This interface describes the manager of symbolic names of STG components. 
 */
public interface StgTextRefMan {
	/**
	 * Assigns the given new name for the given node.
	 * An exception will be thrown in case the name is invalid.
	 * @param node
	 * The node to be given the name
	 * @param newName
	 * The name to be given to the node
	 */
	public void setName(Node node, String newName);
	/**
	 * Returns an implementation of ReferenceManager interface to use in Model interface implementation  
	 * @return
	 */
	public Expression<? extends ReferenceManager> referenceManager();
}
