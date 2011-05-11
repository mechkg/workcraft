package org.workcraft.plugins.cpog.optimisation.expressions;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;

public class Variable<Var> implements BooleanFormula<Var> {

	private Variable(Var variable) {
		this.variable = variable;
	}
	
	public static <Var> Variable<Var> create(Var var) {
		return new Variable<Var>(var);
	}

	private Var variable;

	@Override
	public int hashCode() {
		return variable.hashCode();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Variable other = (Variable) obj;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}
	
	public Var variable() {
		return variable;
	}
	
	@Override
	public <T> T accept(BooleanVisitor<Var, T> visitor) {
		return visitor.visit(this);
	}
}
