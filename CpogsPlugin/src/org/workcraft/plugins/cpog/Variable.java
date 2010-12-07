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

package org.workcraft.plugins.cpog;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;

@DisplayName("Variable")
@VisualClass("org.workcraft.plugins.cpog.VisualVariable")
public class Variable extends MathNode implements Comparable<Variable>, BooleanVariable
{
	private final ModifiableExpression<VariableState> state = new org.workcraft.dependencymanager.advanced.user.Variable<VariableState>(VariableState.UNDEFINED);
	
	private final ModifiableExpression<String> label = new org.workcraft.dependencymanager.advanced.user.Variable<String>("");

	public ModifiableExpression<VariableState> state()
	{
		return state;
	}	

	@Override
	public int compareTo(Variable o)
	{		
		return getLabel().compareTo(o.getLabel());
	}

	public ModifiableExpression<String> label()
	{
		return label;
	}
	
	@Override
	public String getLabel() {
		return eval(label);
	}

	@Override
	public <T> T accept(BooleanVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
