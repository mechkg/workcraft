package org.workcraft.plugins.cpog.scala.nodes.snapshot

import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.exceptions.NotImplementedException
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.VariableReplacer
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.plugins.cpog.scala.{nodes => M}
import org.workcraft.plugins.cpog.scala.Expressions._

import scala.collection.immutable.Map

class SnapshotMaker {
  
  class MonadExtendedList[A] (self : List[A]) {
	  def mapE[B](f : A => Expression[B]) : Expression[List[B]] = joinCollection[B] (self.map(f))
  }
  
  implicit def extendMonadic[A](list : List[Expression[A]]) = new MonadExtendedList(list)
  
  
  var snapshots = Map.empty[M.Node, Node];
  
	def makeSnapshot(nodes : List[M.Node]) : Expression[CPOG] = {
	  CPOG(nodes.mapE(makeSnapshot(_ : M.Node)))
	}
	
	def makeSnapshot(node : M.Node) : Node = {
	  snapshots.getOrElse(node, { // 
		  val result = node match {
		    case c : M.Component => makeSnapshot(c) 
		    case a : M.Arc => makeSnapshot(a) 
		  }
		  if(snapshots.contains(node)) throw new NotImplementedException("Need to ensure this never happens!");
		  snapshots += ((node, result));
		  result;
		})
	}
	
	def makeSnapshot(component : M.Component) : Expression[Component] = {
	  component match {
	    case M.Vertex(condition, visualProperties) => for(condition <- condition) yield new Vertex(makeSnapshot(condition), makeSnapshot(visualProperties))
	  }
	}
	
	def makeSnapshot(condition : BooleanFormula[M.Variable]) : BooleanFormula[Variable] = {
	  VariableReplacer.replace((v : M.Variable) => makeSnapshot(v), condition)
	}
}
