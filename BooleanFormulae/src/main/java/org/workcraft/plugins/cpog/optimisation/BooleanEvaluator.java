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

import org.workcraft.plugins.cpog.optimisation.expressions.And;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.Iff;
import org.workcraft.plugins.cpog.optimisation.expressions.Imply;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.expressions.Variable;
import org.workcraft.plugins.cpog.optimisation.expressions.Xor;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

public class BooleanEvaluator<Var> extends BooleanVisitor<Var, Boolean> 
{

	@Override
	public Boolean visit(And<Var> node) {
		return apply(node.getX()) && apply(node.getY());
	}

	@Override
	public Boolean visit(Iff<Var> node) {
		return apply(node.getX()).equals(apply(node.getY()));
	}

	@Override
	public Boolean visit(Zero<Var> node) {
		return false;
	}

	@Override
	public Boolean visit(One<Var> node) {
		return true;
	}

	@Override
	public Boolean visit(Not<Var> node) {
		return !apply(node.getX());
	}

	@Override
	public Boolean visit(Imply<Var> node) {
		return !apply(node.getX()) || apply(node.getY());
	}

	@Override
	public Boolean visit(Variable<Var> variable) {
		throw new RuntimeException("Unable to evaluate a function containing a free variable: " + variable.toString());
	}

	@Override
	public Boolean visit(Or<Var> node) {
		return apply(node.getX()) || apply(node.getY());
	}

	@Override
	public Boolean visit(Xor<Var> node) {
		return apply(node.getX()) ^ apply(node.getY());
	}
}
