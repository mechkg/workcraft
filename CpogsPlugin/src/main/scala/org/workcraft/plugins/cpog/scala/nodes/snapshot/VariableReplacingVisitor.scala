import org.workcraft.plugins.cpog.optimisation.expressions.GenericBooleanVisitor

package org.workcraft.plugins.cpog.scala.nodes.snapshot

class VariableReplacingVisitor[V1, V2] extends GenericBooleanVisitor[V1, GenericBooleanFormula[V2]] {
	override def GenericBooleanFormula[V2] visit(And and) = 
	public T visit(Or node);
	public T visit(Iff node);
	public T visit(Xor node);
	public T visit(Zero node);
	public T visit(One node);
	public T visit(Not node);
	public T visit(Imply node);
	
	public T visit(Var variable);
}