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

import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer;
import org.workcraft.util.Function;

public class Optimiser<Var, BooleanNumber> implements CpogSATProblemGenerator<Var, BooleanFormula<Var>>
{
	final Function<String, Var> variableGenerator;
	
	BooleanFormula<Var> generateBinaryFunction(List<BooleanFormula<Var>> vars, int funcId)
	{
		return generateBinaryFunction(vars, vars, funcId);
	}
	
	BooleanFormula<Var> generateBinaryFunction(List<BooleanFormula<Var>> arg1, List<BooleanFormula<Var>> arg2, int funcId)
	{
		BooleanFormula<Var> isIff = zero();//*/new FV("f"+funcId + "_isIff");
		BooleanNumber var1Number = generateInt("f"+funcId + "_v1_", arg1.size());
		BooleanNumber var2Number = generateInt("f"+funcId + "_v2_", arg2.size());
		//BooleanFormula<Var> less = numberProvider.less(var1Number, var2Number);
		//rho.add(less);
		BooleanFormula<Var> noNegate1 = var(variableGenerator.apply("f"+funcId + "_v1_plain"));
		BooleanFormula<Var> noNegate2 = var(variableGenerator.apply("f"+funcId + "_v2_plain"));
		BooleanFormula<Var> var1 = numberProvider.select(arg1, var1Number);
		BooleanFormula<Var> var2 = numberProvider.select(arg2, var2Number);
		
		//noNegate1 = ZERO;
		//noNegate1 = ZERO;
		if(funcId == 0)
		{
			var1 = arg1.get(0);
			var2 = arg2.get(1);
			//noNegate1 = ZERO;
			//noNegate2 = ZERO;
		}
		
		if(funcId == 1)
		{
			var1 = arg1.get(1);
			var2 = arg2.get(2);
			//noNegate1 = ZERO;
		}
		if(funcId == 2)
		{
			var1 = arg1.get(2);
			var2 = arg2.get(3);
			//noNegate1 = ZERO;
		}
		if(funcId == 3)
		{
			var1 = arg1.get(3);
			var2 = arg2.get(0);
			//noNegate1 = ZERO;
		}
		/*if(funcId == 4)
		{
			var1 = arg1[2];
			var2 = arg2[0];
			//noNegate1 = ZERO;
		}*/
		//noNegate1 = ZERO;
		//noNegate2 = ZERO;
		
		BooleanFormula<Var> and = and(
				iff(var1, noNegate1), 
				iff(var2, noNegate2)
				);

		//if(true)
		//	return not(and(var1,var2));
		BooleanFormula<Var> iff = iff(var1, var2);
		
		return or(
			and(isIff, iff),
			and(not(isIff), and)
		);
	}
	
	private BooleanNumber generateInt(String varPrefix, int variablesCount) {
		return numberProvider.generate(varPrefix, variablesCount);
	}

	NumberProvider<Var, BooleanNumber> numberProvider;
	private int[] levels;
	
	public Optimiser(Function<String, Var> variableGenerator, NumberProvider<Var, BooleanNumber> numberProvider)
	{
		this.numberProvider = numberProvider;
		this.variableGenerator = variableGenerator;
	}
	
	public Optimiser(Function<String, Var> variableGenerator, NumberProvider<Var, BooleanNumber> numberProvider, int [] levels)
	{
		this(variableGenerator, numberProvider);
		this.levels = levels;
	}
	
	public CpogOptimisationTask<Var, BooleanFormula<Var>> getFormula(String [] scenarios, int freeVarsCount, int derivedVariables)
	{
		Map<Character, Var> forcedVariables = new HashMap<Character, Var>();
		
		List<List<BooleanFormula<Var>>> parsedMatrix = new ArrayList<List<BooleanFormula<Var>>>(); 
		
		for(int i=0;i<scenarios.length;i++)
		{
			String s = scenarios[i];
			ArrayList<BooleanFormula<Var>> line = new ArrayList<BooleanFormula<Var>>();
			for(int j=0;j<s.length();j++)
			{
				Character c = s.charAt(j);
				BooleanFormula<Var> cell;
				if(c == '1')
					cell = one();
				else
					if(c == '0')
						cell = one();
					else
						if(c == '-')
							cell = null;
						else
						{
							boolean upper = false;
							if(c>='A' && c<='Z')
							{
								upper = true;
								c = Character.toLowerCase(c);
							}

							if(c>='a' && c<='z')
							{
								cell = var(forcedVariables.get(c));
								if(cell == null)
								{
									Var var = variableGenerator.apply(c.toString());
									forcedVariables.put(c, var);
									cell = var(var);
								}
								if(upper)
									cell = not(cell);
							}
							else
								throw new RuntimeException("unknown character: " + c);
						}
				line.add(cell);
			}
			parsedMatrix.add(line);
		}

		List<BooleanFormula<Var>> forcedParams = new ArrayList<BooleanFormula<Var>>();
		for(Var var : forcedVariables.values())
			forcedParams.add(var(var));
		
		CpogOptimisationTask<Var, BooleanFormula<Var>> preResult = getFormula(parsedMatrix, forcedParams, freeVarsCount, derivedVariables);
		
		List<List<BooleanFormula<Var>>> vars = preResult.getEncodingVars();
		List<BooleanFormula<Var>> funcs = preResult.getFunctionVars();
		BooleanFormula<Var> result = preResult.getTask();
		
		for(Var v : forcedVariables.values())
			result = eliminateUnrestrictableVar(result, v);

		return new CpogOptimisationTask<Var, BooleanFormula<Var>>(funcs, vars, result);
	}

	private BooleanFormula<Var> eliminateUnrestrictableVar(BooleanFormula<Var> result, Var v) {
		//System.out.println("original: " + FormulaToString.toString(result));

		BooleanFormula<Var> one = replace(result, v, worker.<Var>one());
		BooleanFormula<Var> zero = replace(result, v, worker.<Var>zero());
		//System.out.println("one: " + FormulaToString.toString(one));
		//System.out.println("zero: " + FormulaToString.toString(zero));
		return and(one, zero);
	}

	private BooleanFormula<Var> replace(BooleanFormula<Var> where, final Var what, final BooleanFormula<Var> with) 
	{
		return BooleanReplacer.cachedReplacer(worker, new Function<Var, BooleanFormula<Var>>(){
			@Override
			public BooleanFormula<Var> apply(Var var) {
				return var.equals(what) ? with : var(var);
			}
		}).apply(where);
	}

	public CpogOptimisationTask<Var, BooleanFormula<Var>> getFormula(List<List<BooleanFormula<Var>>> scenarios, List<BooleanFormula<Var>> forcedParams, int freeVarsCount, int derivedVariables)
	{
		// Generate function parameters
		List<Var> freeVariables = new ArrayList<Var>();
		char nextVar = 'x';
		for(int i=0;i<freeVarsCount;i++)
		{
			freeVariables.add(variableGenerator.apply(""+(nextVar++)));
			if(nextVar>'z')
				nextVar = 'p';
			if(nextVar == 'x')
				nextVar = 'a';
		}

		List<BooleanFormula<Var>> parameters = new ArrayList<BooleanFormula<Var>>();
		for(Var variable : freeVariables)
			parameters.add(var(variable));
		parameters.addAll(forcedParams);
		
		// Generate functions
		List<BooleanFormula<Var>> allVariables = generateFunctions(parameters, derivedVariables);
		
		int functionCount = scenarios.get(0).size();
		
		List<BooleanFormula<Var>> cpogFunctions = new ArrayList<BooleanFormula<Var>>();
		
		List<BooleanFormula<Var>> tableConditions = new ArrayList<BooleanFormula<Var>>();
		//Try to match CPOG functions with generated functions.
		for(int i=0;i<functionCount;i++)
		{
			BooleanNumber varId = generateInt("cpog_f"+i+"_",allVariables.size());
			BooleanFormula<Var> plain = var(variableGenerator.apply("cpog_f"+i+"_plain"));
			
			BooleanFormula<Var> value = iff(plain, numberProvider.select(allVariables, varId));
			
			cpogFunctions.add(value);
		}

		//Generate all possible encodings...
		List<List<BooleanFormula<Var>>> encodings = new ArrayList<List<BooleanFormula<Var>>>();
		for(int i=0;i<scenarios.size();i++)
		{
			List<BooleanFormula<Var>> encoding = new ArrayList<BooleanFormula<Var>>();
			if(i == 0)
			{
				for(int j=0;j<freeVarsCount;j++)
					encoding.add(worker.<Var>zero());
/*				for(int j=0;j<0 && j<freeVarsCount;j++) wtf is this code??
					encodings[i][j] = new FreeVariable("x"+j+"_s"+i); */
			}
			else
				for(int j=0;j<freeVarsCount;j++)
					encoding.add(var(variableGenerator.apply("x"+j+"_s"+i)));
			encodings.add(encoding);
		}
		
		//Verify results
		for(int i=0;i<functionCount;i++) {
			BooleanFormula<Var> value = cpogFunctions.get(i);
			for(int j=0;j<scenarios.size();j++) {
				BooleanFormula<Var> substituted = BooleanReplacer.replace(worker, value, freeVariables, encodings.get(j));
				
				BooleanFormula<Var> required = scenarios.get(j).get(i);
				if(required != null)
					tableConditions.add(iff(required, substituted));
			}
		}
		
		tableConditions.add(numberProvider.getConstraints());
		
		return new CpogOptimisationTask<Var, BooleanFormula<Var>>(cpogFunctions, encodings, and(tableConditions));
	}

	private List<BooleanFormula<Var>> generateFunctions(List<BooleanFormula<Var>> parameters, int functionCount) 
	{
		List<BooleanFormula<Var>> allVariables = new ArrayList<BooleanFormula<Var>>(parameters);
		List<BooleanFormula<Var>> lastLevel = new ArrayList<BooleanFormula<Var>>(parameters);
		
		if(levels != null)
		{
			if(levels.length == 1 && levels[0]==-1)
			{
				for(int i=0;i<parameters.size();i++)
					for(int j=i+1;j<parameters.size();j++)
					{
						BooleanFormula<Var> param1 = parameters.get(i);
						BooleanFormula<Var> param2 = parameters.get(j);
						for(int p=0;p<2;p++)
							for(int q=0;q<2;q++)
							{
								BooleanFormula<Var> arg1 = p!=0 ? not(param1) : param1;
								BooleanFormula<Var> arg2 = q!=0 ? not(param2) : param2;
								allVariables.add(and(arg1, arg2));
							}
						allVariables.add(iff(param1, param2));
					}
			}
			else
			{
				int cc = 0;
				for(int level = 0;level<levels.length;level++)
				{
					List<BooleanFormula<Var>> currentLevel = new ArrayList<BooleanFormula<Var>>();
					if(levels[level] == 0)
						throw new RuntimeException("wtf?");
					for(int i=0;i<levels[level];i++)
					{
						final List<BooleanFormula<Var>> firstArgPool;
						int firstArgPoolSize = i*2+1;
						if(firstArgPoolSize > lastLevel.size())
							firstArgPool = lastLevel;
						else {
							firstArgPool = new ArrayList<BooleanFormula<Var>>();
							for(int k=0;k<firstArgPoolSize;k++)
								firstArgPool.add(lastLevel.get(k));
						}
						BooleanFormula<Var> function = generateBinaryFunction(firstArgPool, allVariables, cc);
						cc++;
						currentLevel.add(function);
					}
					allVariables.addAll(currentLevel);
					lastLevel = currentLevel;
				}
			}
		}
		else
		{
			//Generate all possible functions.
			for(int i=0;i<functionCount;i++)
			{
				BooleanFormula<Var> function = generateBinaryFunction(allVariables, i);
				allVariables.add(function);
			}
		}
		return allVariables;
	}

	public static <Var, BooleanNumber> BooleanFormula<Var> evaluateNoNeg(NumberProvider<Var, OneHotIntBooleanFormula<Var>> numberProvider, AndFunction<OneHotIntBooleanFormula<Var>> function, List<BooleanFormula<Var>> vars) {
		return and(
				numberProvider.select(vars, function.getVar1Number()),
				numberProvider.select(vars, function.getVar2Number())
				);
	}
}
