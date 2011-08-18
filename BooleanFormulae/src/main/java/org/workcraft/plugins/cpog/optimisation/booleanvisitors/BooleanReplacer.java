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

import java.util.HashMap;
import java.util.List;

import org.workcraft.plugins.cpog.optimisation.BinaryBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.expressions.And;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.Iff;
import org.workcraft.plugins.cpog.optimisation.expressions.Imply;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.expressions.Xor;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.plugins.cpog.optimisation.expressions.Variable;
import org.workcraft.util.Function;
import org.workcraft.util.Memoise;

import static org.workcraft.plugins.cpog.optimisation.booleanvisitors.Fix.*;


public class BooleanReplacer<Var1, Var2> extends BooleanVisitor<Var1, BooleanFormula<Var2>> implements Function<BooleanFormula<Var1>, BooleanFormula<Var2>>
{
	private final Function<Var1, BooleanFormula<Var2>> replacer;
	private final Function<BooleanFormula<Var1>, BooleanFormula<Var2>> recurse;
	private final BooleanWorker worker;

	private static <Var1, Var2> HashMap<Var1, BooleanFormula<Var2>> createMap(List<Var1> from, List<BooleanFormula<Var2>> to) {
		HashMap<Var1, BooleanFormula<Var2>> m = new HashMap<Var1, BooleanFormula<Var2>>();
		if(from.size() != to.size())
			throw new RuntimeException("lengths must be equal");
		for(int i=0;i<from.size();i++)
			m.put(from.get(i), to.get(i));
		return m;
	}
	
	public static <Var1, Var2> Function<BooleanFormula<Var1>, BooleanFormula<Var2>> cachedReplacer(final BooleanWorker worker, final Function<Var1, BooleanFormula<Var2>> f) {
		return fix(new Function<Function<BooleanFormula<Var1>, BooleanFormula<Var2>>, Function<BooleanFormula<Var1>, BooleanFormula<Var2>>>() {
			@Override
			public Function<BooleanFormula<Var1>, BooleanFormula<Var2>> apply(Function<BooleanFormula<Var1>, BooleanFormula<Var2>> rec) {
				return new BooleanReplacer<Var1, Var2>(rec, worker, Memoise.memoise(f));
			}
		});
	}
	

	public static <Var> BooleanFormula<Var> replace(final BooleanWorker worker, BooleanFormula<Var> formula, List<Var> params, List<BooleanFormula<Var>> values) {
		final HashMap<Var, BooleanFormula<Var>> map = createMap(params, values);
		return cachedReplacer(worker, new Function<Var, BooleanFormula<Var>>(){

			@Override
			public BooleanFormula<Var> apply(Var argument) {
				BooleanFormula<Var> res = map.get(argument);
				if(res != null)
					return res;
				else
					return worker.var(argument);
			}
			
		}).apply(formula);
	}
	
	public BooleanReplacer(Function<BooleanFormula<Var1>, BooleanFormula<Var2>> recurse, BooleanWorker worker, Function<Var1, BooleanFormula<Var2>> replacer) {
		this.recurse = recurse;
		this.worker = worker;
		this.replacer = replacer;
	}

	protected BooleanFormula<Var2> visitBinaryFunc(BinaryBooleanFormula<Var1> node, BinaryOperation op) {
		BooleanFormula<Var2> x = recurse.apply(node.getX());
		BooleanFormula<Var2> y = recurse.apply(node.getY());
		return op.apply(x, y);
	}
	
	interface BinaryOperation
	{
		public <Var> BooleanFormula<Var> apply(BooleanFormula<Var> x, BooleanFormula<Var> y);
	}
	
	@Override
	public BooleanFormula<Var2> visit(And<Var1> node) {
		return visitBinaryFunc(node, new BinaryOperation()
		{
			@Override
			public <V> BooleanFormula<V> apply(BooleanFormula<V> x, BooleanFormula<V> y) {
				return worker.and(x,y);
			}
		});
	}
	
	@Override
	public BooleanFormula<Var2> visit(Or<Var1> node) {
		return visitBinaryFunc(node, new BinaryOperation()
		{
			@Override
			public <V> BooleanFormula<V> apply(BooleanFormula<V> x, BooleanFormula<V> y) {
				return worker.or(x,y);
			}
		});
	}
	
	@Override
	public BooleanFormula<Var2> visit(Variable<Var1> var) {
		return replacer.apply(var.variable());
	}

	@Override
	public BooleanFormula<Var2> visit(Iff<Var1> node) {
		return visitBinaryFunc(node, new BinaryOperation()
		{
			@Override
			public <V> BooleanFormula<V> apply(BooleanFormula<V> x, BooleanFormula<V> y) {
				return worker.iff(x,y);
			}
		});
	}

	@Override
	public BooleanFormula<Var2> visit(Xor<Var1> node) {
		return visitBinaryFunc(node, new BinaryOperation()
		{
			@Override
			public <V> BooleanFormula<V> apply(BooleanFormula<V> x, BooleanFormula<V> y) {
				return worker.xor(x,y);
			}
		});
	}

	@Override
	public BooleanFormula<Var2> visit(Zero<Var1> node) {
		return Zero.instance();
	}

	@Override
	public BooleanFormula<Var2> visit(One<Var1> node) {
		return One.instance();
	}

	@Override
	public BooleanFormula<Var2> visit(Not<Var1> node) {
		BooleanFormula<Var2> x = recurse.apply(node.getX());
		return worker.not(x);
	}

	@Override
	public BooleanFormula<Var2> visit(Imply<Var1> node) {
		return visitBinaryFunc(node, new BinaryOperation()
		{
			@Override
			public <V> BooleanFormula<V> apply(BooleanFormula<V> x, BooleanFormula<V> y) {
				return worker.imply(x,y);
			}
		});
	}
}
