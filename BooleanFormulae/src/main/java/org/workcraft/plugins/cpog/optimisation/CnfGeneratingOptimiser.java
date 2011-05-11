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

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.cpog.optimisation.expressions.BooleanWorker;
import org.workcraft.util.Function;
import org.workcraft.util.Function2;

import static org.workcraft.plugins.cpog.optimisation.CnfOperations.*;

public class CnfGeneratingOptimiser<Var> implements CpogSATProblemGenerator<Var, Cnf<Var>>
{
	private List<CnfClause<Var>> rho = new ArrayList<CnfClause<Var>>();
	private final Function<String, Var> variableGenerator;

	TwoHotRange<Var> generateBinaryFunction(int variablesCount, int funcId)
	{
		TwoHotRangeProvider<Var> prov = new TwoHotRangeProvider<Var>(variableGenerator);
		TwoHotRange<Var> result = prov.generate("f"+funcId, variablesCount);
		rho.addAll(prov.getConstraints().getClauses());
		return result;
	}
	
	private OneHotIntBooleanFormula<Var> generateInt(String varPrefix, int variablesCount) {
		return numberProvider.generate(varPrefix, variablesCount);
	}
	
	void assignValue(Literal<Var> literal, boolean value) {
		rho.add(or(value ? literal : not(literal)));
	}
	
	Literal<Var> createConstant(boolean value) {
		Literal<Var> res = literal(variableGenerator.apply(value?"1":"0"));
		assignValue(res, value);
		return res;
	}

	CnfGeneratingOneHotNumberProvider<Var> numberProvider;
	private final Function2<Var, String, Var> subvariableGenerator;
	private final BooleanWorker worker;
	private final Literal<Var> one;
	private final Literal<Var> zero;
	
	public CnfGeneratingOptimiser(BooleanWorker worker, final Function<String, Var> variableGenerator, final Function<Var, String> variableNameProvider)
	{
		this.worker = worker;
		this.variableGenerator = variableGenerator;
		this.numberProvider = new CnfGeneratingOneHotNumberProvider<Var>(variableGenerator);
		this.subvariableGenerator = new Function2<Var, String, Var>() {

			@Override
			public Var apply(Var argument1, String argument2) {
				return variableGenerator.apply(variableNameProvider.apply(argument1) + argument2);
			}
		};
		this.zero = createConstant(false);
		this.one = createConstant(true);
	}
	
	public CpogOptimisationTask<Var, Cnf<Var>> getFormula(String [] scenarios, int freeVariables, int derivedVariables)
	{
		int nonDerivedVariables = freeVariables;
		
		//Generate all possible encodings...
		List<List<Literal<Var>>> encodings = new ArrayList<List<Literal<Var>>>();
		for(int i=0;i<scenarios.length;i++)
		{
			List<Literal<Var>> encoding = new ArrayList<Literal<Var>>();
			if(i == 0)
				for(int j=0;j<freeVariables;j++)
					encoding.add(zero);
			else
				for(int j=0;j<freeVariables;j++)
					encoding.add(literal(variableGenerator.apply("x"+j+"_s"+i)));
			encodings.add(encoding);
		}
		
		//... and all possible functions.
		List<TwoHotRange<Var>> derivedFunctions = new ArrayList<TwoHotRange<Var>>();
		for(int i=0;i<derivedVariables;i++)
			derivedFunctions.add(generateBinaryFunction((nonDerivedVariables/*+i*/)*2, i));
		
		orderFunctions(derivedFunctions);
		
		//Evaluate all functions for all scenarios.
		List<List<Literal<Var>>> functionSpace = new ArrayList<List<Literal<Var>>>();
		int totalVariables = nonDerivedVariables*2 + derivedVariables*2;
		for(int i=0;i<scenarios.length;i++)
		{
			List<Literal<Var>> function = new ArrayList<Literal<Var>>();
			for(int j=0;j<nonDerivedVariables;j++)
			{
				Literal<Var> ij = encodings.get(i).get(j);
				function.add(ij);
				function.add(not(ij));
			}
			for(int j=0;j<derivedVariables;j++)
			{
				List<Literal<Var>> availableFormulas = new ArrayList<Literal<Var>>();

				for(int k=0;k</*j+*/nonDerivedVariables;k++)
				{
					availableFormulas.add(function.get(k*2));
					availableFormulas.add(function.get(k*2+1));
				}
				
				Literal<Var> value = literal(variableGenerator.apply("f"+j+"_s"+i));
				selectAnd(value, derivedFunctions.get(j), availableFormulas);
				function.add(value);
				function.add(not(value));
			}
			functionSpace.add(function);
		}
		
		int functionCount = scenarios[0].length();
		
		List<CnfClause<Var>> tableConditions = new ArrayList<CnfClause<Var>>();
		
		List<OneHotIntBooleanFormula<Var>> cpogSelections = new ArrayList<OneHotIntBooleanFormula<Var>>();
		//Try to match CPOG functions with generated functions.
		for(int i=0;i<functionCount;i++)
		{
			OneHotIntBooleanFormula<Var> varId = generateInt("cpog_f"+i+"_",totalVariables);
			cpogSelections.add(varId);
			for(int j=0;j<scenarios.length;j++)
			{
				boolean inverse;
				char ch = scenarios[j].charAt(i);
				if(ch=='-')
					continue;
				else if(ch=='1')
					inverse = false;
				else if(ch=='0')
					inverse = true;
				else throw new RuntimeException("unknown symbol: " + parseBoolean(ch));

				List<CnfClause<Var>> value = select(functionSpace.get(j), varId, inverse);
				
				tableConditions.addAll(value);
			}
		}
		
		List<CnfClause<Var>> numberConstraints = numberProvider.getConstraintClauses();
		tableConditions.addAll(numberConstraints);
		tableConditions.addAll(rho);
		
		// Forming solution output here
		List<Var> parameters = new ArrayList<Var>(); 
		Character varName = 'a';
		for(int i=0;i<nonDerivedVariables;i++)
		{
			parameters.add(variableGenerator.apply(varName.toString()));
			varName++;
		}
		
		List<BooleanFormula<Var>> funcs = new ArrayList<BooleanFormula<Var>>();
		for(int j=0;j<nonDerivedVariables;j++)
		{
			BooleanFormula<Var> par = worker.var(parameters.get(j));
			funcs.add(par);
			funcs.add(worker.not(par));
		}
		for(int j=0;j<derivedVariables;j++)
		{
			List<BooleanFormula<Var>> availableFormulas = new ArrayList<BooleanFormula<Var>>();

			for(int k=0;k</*j+*/nonDerivedVariables;k++)
			{
				availableFormulas.add(funcs.get(k*2));
				availableFormulas.add(funcs.get(k*2+1));
			}
			BooleanFormula<Var> value = TwoHotRangeProvider.<Var>selectAnd(availableFormulas, derivedFunctions.get(j));
			funcs.add(value);
			funcs.add(worker.not(value));
		}
		
		List<BooleanFormula<Var>> functionVars = new ArrayList<BooleanFormula<Var>>(); 
		for(int i=0;i<functionCount;i++) {
			functionVars.add(numberProvider.select(funcs, cpogSelections.get(i)));
		}
		
		List<List<BooleanFormula<Var>>> enc = new ArrayList<List<BooleanFormula<Var>>>();
		for(int i=0;i<encodings.size();i++) {
			ArrayList<BooleanFormula<Var>> en = new ArrayList<BooleanFormula<Var>>();
			for(int j=0;j<encodings.get(i).size();j++)
				en.add(encodings.get(i).get(j));
			enc.add(en);
		}
		
		return new CpogOptimisationTask<Var, Cnf<Var>>(functionVars,enc,new Cnf<Var>(tableConditions));
	}

	private void orderFunctions(List<TwoHotRange<Var>> derivedFunctions) {
		if(derivedFunctions.size()>0)
		{
			rho.add(or(derivedFunctions.get(0).get(0),derivedFunctions.get(0).get(1)));
			rho.add(or(derivedFunctions.get(0).get(2),derivedFunctions.get(0).get(3)));
			if(derivedFunctions.size()>1)
			{
				//rho.add(or(derivedFunctions[1].get(0),derivedFunctions[1].get(1),derivedFunctions[1].get(2),derivedFunctions[1].get(3),derivedFunctions[1].get(4),derivedFunctions[1].get(5)));
				//rho.add(or(derivedFunctions[1].get(0),derivedFunctions[1].get(1),derivedFunctions[1].get(2),derivedFunctions[1].get(3),derivedFunctions[1].get(4),derivedFunctions[1].get(5)));
			}
		}
		
		for(int i=0;i<derivedFunctions.size();i++)
		{
			int bits = derivedFunctions.get(i).size();
			for(int j=i+1;j<derivedFunctions.size();j++)
			{
				int bitsj = derivedFunctions.get(j).size();
				if(bits != bitsj)
					throw new RuntimeException("Functions have different widths: "+bits+" and " + bitsj);
				List<Literal<Var>> si = derivedFunctions.get(i).getThermometer();
				List<Literal<Var>> xj = derivedFunctions.get(j);
				for(int k=0;k<bits;k++)
				{
					rho.add(or(si.get(k), not(xj.get(k))));
				}
			}
		}
	}

	private Literal<Var> parseBoolean(char ch) {
		if(ch == '0')
			return zero;
		else
			if(ch=='1')
				return one;
			else
				throw new RuntimeException("o_O");
	}

	@SuppressWarnings("unused")
	private void evaluate(Literal<Var> result, AndFunction<OneHotIntBooleanFormula<Var>> function, List<Literal<Var>> f) 
	{
		Literal<Var> sel1 = literal(subvariableGenerator.apply(result.getVariable(), "_sel1"));
		Literal<Var> sel2 = literal(subvariableGenerator.apply(result.getVariable(), "_sel2"));
		OneHotIntBooleanFormula<Var> x = function.getVar1Number();
		OneHotIntBooleanFormula<Var> y = function.getVar2Number();
		List<CnfClause<Var>> sel1C = CnfGeneratingOneHotNumberProvider.<Var>select(sel1, f, x);
		List<CnfClause<Var>> sel2C = CnfGeneratingOneHotNumberProvider.<Var>select(sel2, f, y);
		
		rho.add(or(result, not(sel1), not(sel2)));
		rho.add(or(not(result), sel1));
		rho.add(or(not(result), sel2));
		rho.addAll(sel1C);
		rho.addAll(sel2C);
	}

	private void selectAnd(Literal<Var> result, TwoHotRange<Var> function, List<Literal<Var>> f) 
	{
		List<CnfClause<Var>> sel = TwoHotRangeProvider.<Var>selectAnd(subvariableGenerator, result, f, function);
		rho.addAll(sel);
	}

	private List<CnfClause<Var>> select(List<Literal<Var>> vars, OneHotIntBooleanFormula<Var> number, boolean inverse) {
		return CnfGeneratingOneHotNumberProvider.<Var>select(vars, number, inverse);
	}
}
