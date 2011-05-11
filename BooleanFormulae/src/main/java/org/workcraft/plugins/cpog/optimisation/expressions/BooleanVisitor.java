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
package org.workcraft.plugins.cpog.optimisation.expressions;

import org.workcraft.util.Function;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;

public abstract class BooleanVisitor<Var, T> implements Function<BooleanFormula<Var>, T> {
	public abstract T visit(And<Var> node);
	public abstract T visit(Iff<Var> node);
	public abstract T visit(Xor<Var> node);
	public abstract T visit(Zero<Var> node);
	public abstract T visit(One<Var> node);
	public abstract T visit(Not<Var> node);
	public abstract T visit(Imply<Var> node);
	
	public abstract T visit(Variable<Var> variable);
	public abstract T visit(Or<Var> node);
	
	public T apply(BooleanFormula<Var> var) {
		return var.accept(this);
	}
}
