package org.workcraft.dom;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.workcraft.util.Func;

public class DependantRemovingHierarchyController implements HierarchyController {
	
	private final Func<Node, Collection<Node>> dependants;
	private final HierarchyController next;

	public DependantRemovingHierarchyController(HierarchyController next, Func<Node, Collection<Node>> dependants) {
		this.next = next;
		this.dependants = dependants;
	}
	
	private void recursiveDelete(LinkedHashSet<Node> deletionList, Node node) {
		Collection<Node> dependants = this.dependants.eval(node);
		for(Node n : dependants)
			if(!deletionList.contains(n))
				recursiveDelete(deletionList, n);
		
		deletionList.add(node);
	}
	
	@Override
	public void add(Container parent, Node node) {
		next.add(parent, node);	
	}

	@Override
	public void remove(Node node) {
		LinkedHashSet<Node> deletionList = new LinkedHashSet<Node>(); 
		recursiveDelete(deletionList, node);
			
		for(Node n : deletionList)
			next.remove(n);
	}
}
