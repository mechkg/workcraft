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

import static org.workcraft.plugins.cpog.optimisation.CnfOperations.literal;
import static org.workcraft.plugins.cpog.optimisation.CnfOperations.not;
import static org.workcraft.plugins.cpog.optimisation.CnfOperations.or;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.workcraft.plugins.cpog.optimisation.booleanvisitors.Fix;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.RecursiveBooleanVisitor;
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
import org.workcraft.util.Function;
import org.workcraft.util.Function0;
import org.workcraft.util.Memoise;

public class CleverCnfGenerator<Var> extends BooleanVisitor<Var, Literal<Var>> implements RawCnfGenerator<Var, BooleanFormula<Var>> 
{
	Cnf<Var> result = new Cnf<Var>();
	private final Function0<Var> variableGenerator;
	final Function<BooleanFormula<Var>, Literal<Var>> recurse;
	private Function<Var, String> labelProvider;
	
	private static class Void { private Void(){} }
	
	public CleverCnfGenerator(Function0<Var> variableGenerator, Function<BooleanFormula<Var>, Literal<Var>> recurse) {
		this.variableGenerator = variableGenerator;
		this.recurse = recurse;
	}
	
	public static <Var> CleverCnfGenerator<Var> create(final Function0<Var> variableGenerator) {
		return Fix.fix(new Function<Function<BooleanFormula<Var>, Literal<Var>>, CleverCnfGenerator<Var>>(){
			@Override
			public CleverCnfGenerator<Var> apply(Function<BooleanFormula<Var>, Literal<Var>> recurse) {
				return new CleverCnfGenerator<Var>(variableGenerator, Memoise.memoise(recurse));
			}
		});
	}
	
	static class ConstantExpectingCnfGenerator<Var> extends BooleanVisitor<Var, Void>
	{
		private static boolean cleverOptimiseAnd = true;
		
		private BooleanVisitor<Var, Literal<Var>> dumbGenerator;
		private Cnf<Var> result;

		public ConstantExpectingCnfGenerator(Cnf<Var> result, BooleanVisitor<Var, Literal<Var>> dumbGenerator)
		{
			this.result = result;
			this.dumbGenerator = dumbGenerator;
		}
		
		boolean currentResult = true;
		
		@Override
		public Void visit(And<Var> and)
		{
			if(currentResult)
			{
				and.getX().accept(this);
				and.getY().accept(this);
			}
			else
			{
				if(!cleverOptimiseAnd)
				{
					Literal<Var> x = and.getX().accept(dumbGenerator);
					Literal<Var> y = and.getY().accept(dumbGenerator);
					result.add(or(not(x), not(y)));
				}
				else
				{
					List<List<Literal<Var>>> side1 = getBiClause(and.getX());
					List<List<Literal<Var>>> side2 = getBiClause(and.getY());
					for(int i=0;i<side1.size();i++)
						for(int j=0;j<side2.size();j++)
						{
							List<Literal<Var>> list = new ArrayList<Literal<Var>>(); 
							for(int k=0;k<side1.get(i).size();k++)
								list.add(not(side1.get(i).get(k)));
							for(int k=0;k<side2.get(j).size();k++)
								list.add(not(side2.get(j).get(k)));
							result.add(new CnfClause<Var>(list));
						}
				}
					
			}
			return null;
		}

		static class BiClauseGenerator<Var> extends BooleanVisitor<Var, List<List<Literal<Var>>>>
		{
			BooleanVisitor<Var, Literal<Var>> dumbGenerator;
			public BiClauseGenerator(BooleanVisitor<Var, Literal<Var>> dumbGenerator)
			{
				this.dumbGenerator = dumbGenerator;
			}
			@Override
			public List<List<Literal<Var>>> visit(And<Var> node) {
				List<List<Literal<Var>>> result = new ArrayList<List<Literal<Var>>>();
				List<Literal<Var>> clause = new ArrayList<Literal<Var>>();
				clause.add(node.getX().accept(dumbGenerator));
				clause.add(node.getY().accept(dumbGenerator));
				result.add(clause);
				return result;
			}
			@Override
			public List<List<Literal<Var>>> visit(Iff<Var> node) {
				throw new RuntimeException();
			}
			@Override
			public List<List<Literal<Var>>> visit(Zero<Var> node) {
				throw new RuntimeException();
			}
			@Override
			public List<List<Literal<Var>>> visit(One<Var> node) {
				throw new RuntimeException();
			}
			@Override
			public List<List<Literal<Var>>> visit(Not<Var> node) {
				List<List<Literal<Var>>> preres = node.getX().accept(this);
				if(preres.size()!=1)
					throw new RuntimeException("something wrong...");
				List<List<Literal<Var>>> res = new ArrayList<List<Literal<Var>>>();
				for(int i=0;i<preres.get(0).size();i++)
				{
					ArrayList<Literal<Var>> clause = new ArrayList<Literal<Var>>();
					res.add(clause);
					clause.add(CnfOperations.not(preres.get(0).get(i)));
				}
				return res;
			}
			@Override
			public List<List<Literal<Var>>> visit(Imply<Var> node) {
				throw new RuntimeException();
			}
			@Override
			public List<List<Literal<Var>>> visit(Variable<Var> variable) {
				List<List<Literal<Var>>> result = new ArrayList<List<Literal<Var>>>();
				ArrayList<Literal<Var>> clause = new ArrayList<Literal<Var>>();
				clause.add(CnfOperations.literal(variable.variable()));
				result.add(clause);
				return result;
			}
			@Override
			public List<List<Literal<Var>>> visit(Or<Var> node) {
				throw new RuntimeException();
			}
			@Override
			public List<List<Literal<Var>>> visit(Xor<Var> node) {
				throw new RuntimeException();
			}
		}
		
		List<List<Literal<Var>>> getBiClause(BooleanFormula<Var> formula)
		{
			return formula.accept(new BiClauseGenerator<Var>(dumbGenerator));
		}
		
		@Override
		public Void visit(Iff<Var> iff)
		{
			Literal<Var> x = iff.getX().accept(dumbGenerator);
			Literal<Var> y = iff.getY().accept(dumbGenerator);
			if(currentResult)
			{
				result.add(or(x, not(y)));
				result.add(or(not(x), y));
			}
			else
			{
				result.add(or(x, y));
				result.add(or(not(x), not(y)));
			}
			return null;
		}
		
		@Override
		public Void visit(Not<Var> not)
		{
			boolean store = currentResult;
			currentResult = !currentResult;
			not.getX().accept(this);
			currentResult = store;
			return null;
		}

		@Override
		public Void visit(Variable<Var> node) {
			if(currentResult)
				result.add(or(literal(node.variable())));
			else
				result.add(or(not(node.variable())));
			return null;
		}
		@Override
		public Void visit(Zero<Var> node) {
			throw new RuntimeException("not implemented");
		}
		@Override
		public Void visit(One<Var> node) {
			throw new RuntimeException("not implemented");
		}
		@Override
		public Void visit(Imply<Var> node) {
			throw new RuntimeException("not implemented");
		}
		@Override
		public Void visit(Or<Var> node) {
			throw new RuntimeException("not implemented");
		}

		@Override
		public Void visit(Xor<Var> node) {
			throw new RuntimeException("not implemented");
		}
	}
	
	public CnfTask<Var> getCnf(BooleanFormula<Var> formula)
	{
		Cnf<Var> cnf = generateCnf(formula);
		return new SimpleCnfTaskProvider<Var>(labelProvider).getCnf(cnf);
	}

	static class FormulaCounter<Var> extends RecursiveBooleanVisitor<Var, Object>
	{
		int count = 0;
		
		Map<BooleanFormula<Var>, Integer> met = new HashMap<BooleanFormula<Var>, Integer>(); 
		
		@Override
		protected Object visitBinary(BinaryBooleanFormula<Var> node) {
			count++;
			Integer m = met.get(node);
			if(m == null)
				met.put(node,1);
			else
				met.put(node, m+1);
			return super.visitBinary(node);
		}
		
		public void printReport()
		{
			for(Entry<BooleanFormula<Var>, Integer> entry : met.entrySet())
			{
				if(entry.getValue() > 100)
				{
					System.out.println(">1000: " + entry.getValue() + ": " + FormulaToString.printWithToString(entry.getKey()));
				}
			}
		}
		
		public int getCount()
		{
			return count;
		}
		
		public int getUniques()
		{
			return met.size();
		}
	}
	 
	
	public Cnf<Var> generateCnf(BooleanFormula<Var> formula) {
		//FormulaCounter counter = new FormulaCounter();
		//formula.accept(counter);
		//System.out.println("total visits: " + counter.getCount());
		//System.out.println("unique visits: " + counter.getUniques());
		//counter.printReport();
		//System.out.println("formula: " + FormulaToString.toString(formula));
		
		formula.accept(new ConstantExpectingCnfGenerator<Var>(result, this));
		//CnfLiteral res = formula.accept(this);result.add(or(res));
		
		return result;
	}

	Literal<Var> newVar(BooleanFormula<Var> node)
	{
		return literal(variableGenerator.apply());
	}

	interface BinaryGateImplementer<Var>
	{
		void implement(Literal<Var> res, Literal<Var> x, Literal<Var> y);
	}
	
	public Literal<Var> visit(BinaryBooleanFormula<Var> node, BinaryGateImplementer<Var> impl) {
		Literal<Var> res = newVar(node);
		Literal<Var> x = recurse.apply(node.getX());
		Literal<Var> y = recurse.apply(node.getY());
		impl.implement(res, x, y);
		return res;
	}

	@Override
	public Literal<Var> visit(And<Var> node) {
		return visit(node,
			new BinaryGateImplementer<Var>() {
				@Override public void implement(Literal<Var> res, Literal<Var> x, Literal<Var> y) {
					result.add(or(res, not(x), not(y)));
					result.add(or(not(res), x));
					result.add(or(not(res), y));
				}
			}
		);
	}

	@Override
	public Literal<Var> visit(Iff<Var> node) {
		return visit(node,
				new BinaryGateImplementer<Var>() {
					@Override public void implement(Literal<Var> res, Literal<Var> x, Literal<Var> y) {
						result.add(or(not(res), not(x), y));
						result.add(or(not(res), x, not(y)));
						result.add(or(res, not(x), not(y)));
						result.add(or(res, x, y));
					}
				}
			);
	}

	@Override
	public Literal<Var> visit(Zero<Var> node) {
		Literal<Var> zero = newVar(node);
		result.add(or(not(zero)));
		return zero;
	}

	@Override
	public Literal<Var> visit(One<Var> node) {
		Literal<Var> one = newVar(node);
		result.add(or(one));
		return one;
	}

	@Override
	public Literal<Var> visit(Not<Var> node) {
		return not(recurse.apply(node.getX()));
	}

	@Override
	public Literal<Var> visit(Imply<Var> node) {
		return visit(node,
				new BinaryGateImplementer<Var>()
				{
					@Override public void implement(Literal<Var> res, Literal<Var> x, Literal<Var> y) {
						result.add(or(not(res), not(x), y));
						result.add(or(res, not(y)));
						result.add(or(res, x));
					}
				}
			);
	}

	@Override
	public Literal<Var> visit(Variable<Var> variable) {
		return literal(variable.variable());
	}

	@Override
	public Literal<Var> visit(Or<Var> node) {
		return visit(node,
				new BinaryGateImplementer<Var>()
				{
					@Override public void implement(Literal<Var> res, Literal<Var> x, Literal<Var> y) {
						result.add(or(not(res), x, y));
						result.add(or(res, not(y)));
						result.add(or(res, not(x)));
					}
				}
			);
	}

	@Override
	public Literal<Var> visit(Xor<Var> node) {
		return visit(node,
				new BinaryGateImplementer<Var>()
				{
					@Override public void implement(Literal<Var> res, Literal<Var> x, Literal<Var> y) {
						result.add(or(res, not(x), y));
						result.add(or(res, x, not(y)));
						result.add(or(not(res), not(x), not(y)));
						result.add(or(not(res), x, y));
					}
				}
			);
	}
}
