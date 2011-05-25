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
package org.workcraft.plugins.cpog.optimisation.booleanvisitors;

import org.workcraft.plugins.cpog.optimisation.BinaryBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
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


public class RecursiveBooleanVisitor<Var, T> extends BooleanVisitor<Var, T>
{
	protected T visitDefault(BooleanFormula<Var> node)
	{
		return null;
	}
	
	protected T visitBinaryAfterSubnodes(BinaryBooleanFormula<Var> node, T x, T y) {
		return visitDefault(node);
	}
	
	protected T visitBinary(BinaryBooleanFormula<Var> node) {
		return visitBinaryAfterSubnodes(node, node.getX().accept(this), node.getY().accept(this));
	}
	
	@Override
	public T visit(And<Var> node) {
		return visitBinary(node);
	}

	@Override
	public T visit(Iff<Var> node) {
		return visitBinary(node);
	}

	@Override
	public T visit(Zero<Var> node) {
		return visitDefault(node);
	}

	@Override
	public T visit(One<Var> node) {
		return visitDefault(node);
	}

	@Override
	public T visit(Not<Var> node) {
		node.getX().accept(this);
		return visitDefault(node);
	}

	@Override
	public T visit(Imply<Var> node) {
		return visitBinary(node);
	}

	@Override
	public T visit(Variable<Var> node) {
		return visitDefault(node);
	}

	@Override
	public T visit(Or<Var> node) {
		return visitBinary(node);
	}

	@Override
	public T visit(Xor<Var> node) {
		return visitBinary(node);
	}
}
