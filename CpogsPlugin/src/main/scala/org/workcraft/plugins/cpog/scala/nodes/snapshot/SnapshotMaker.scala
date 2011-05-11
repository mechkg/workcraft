package org.workcraft.plugins.cpog.scala.nodes.snapshot

import org.workcraft.plugins.cpog.scala.{nodes => M}

class SnapshotMaker {
  var snapshots = Map.empty[M.Node, Node];
  
	def makeSnapshot(nodes : List[M.Node]) = {
	  CPOG(nodes.map(makeSnapshot(_)))
	}
	
	def makeSnapshot(node : M.Node) : Node = {
	  snapshots.getOrElse(node,() => {
		  val result = node match {
		    case c : M.Component => makeSnapshot(c) 
		    case a : M.Arc => makeSnapshot(a) 
		  }
		  snapshots += ((node, result)); 
		  result;
		})
	}
	
	def makeSnapshot(component : M.Component) = {
	  component match {
	    case M.Vertex(condition, visualProperties) => new Vertex(makeSnapshot(condition), makeSnapshot(visualProperties))
	  }
	}
	
	def makeSnapshot(condition : GenericBooleanFormula[M.Variable]) {
		condition.accept(new VariableReplacingVisitor[M.Variable, Variable] {
			def replaceVariable(v : M.Variable) = makeSnapshot(v)
		})
	}
}
