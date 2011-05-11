/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.workcraft.plugins.cpog.optimisation;

import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.Variable;

public class Literal<Var> implements BooleanFormula<Var>
{
	private Var variable;
	private boolean negation;
	
	public Literal()
	{
	}
	
	public Literal(Var variable) {
		this(variable, false);
	}

	public Literal(Var variable, boolean negation) {
		this.variable = variable;
		this.negation = negation;
	}

	public void setVariable(Var variable) {
		this.variable = variable;
	}

	public Var getVariable() {
		return variable;
	}

	public void setNegation(boolean negation) {
		this.negation = negation;
	}

	public boolean getNegation() {
		return negation;
	}

	@Override
	public <T> T accept(BooleanVisitor<Var, T> visitor) {
		if (!negation)
			return visitor.visit(Variable.create(variable));
		else
			return visitor.visit(Not.create(Variable.create(variable)));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (negation ? 1231 : 1237);
		result = prime * result
				+ ((variable == null) ? 0 : variable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		Literal other = (Literal) obj;
		if (negation != other.negation)
			return false;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}
}
