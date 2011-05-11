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

import static org.workcraft.plugins.cpog.optimisation.CnfOperations.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.util.Function;
import org.workcraft.util.Function2;

public abstract class TwoHotNumberProvider<Var> implements NumberProvider<Var, TwoHotNumber>
{
	final TwoHotRangeProvider<Var> rangeProvider;
	
	Map<Integer, TwoHotRange<Var>> halfTaken = new HashMap<Integer, TwoHotRange<Var>>();
	
	private Cnf<Var> constraints = new Cnf<Var>();

	private final Function<String, Var> variableGenerator;

	public TwoHotNumberProvider(Function<String, Var> variableGenerator) {
		this.rangeProvider = new TwoHotRangeProvider<Var>(variableGenerator);
		this.variableGenerator = variableGenerator;
	}
	
	public Cnf<Var> getConstraints()
	{
		return constraints;
	}
	
	public TwoHotNumber generate(String name, int range)
	{
		TwoHotRange<Var> htRes = halfTaken.get(range);
		if(htRes != null)
		{
			//TwoHotNumber result = rangeProvider.getSecond(htRes);
		}
		
		if(range<2)
			throw new RuntimeException("can't select 2 hot out of "+range);
		
		List<Literal<Var>> literals = createLiterals(variableGenerator, name+"_sel", range);
		List<String> sort1Names = createLiteralNames(name+"_sorta_", range);
		List<Literal<Var>> sort1 = createLiterals(variableGenerator, sort1Names);
		List<Literal<Var>> thermo = createLiterals(variableGenerator, name+"_t_", range);
		List<Literal<Var>> sort2 = createLiterals(variableGenerator, name+"_sortb_", range);
		
		
		constraints.add(CnfSorter.sortRound(sort1, thermo, literals));
		constraints.add(CnfSorter.sortRound(variableGenerator, sort2, sort1, sort1Names));
		
		for(int i=0;i<range-2;i++)
			constraints.add(or(not(sort2.get(i))));

		for(int i=0;i<range-2;i+=2)
			constraints.add(or(not(literals.get(i)), not(literals.get(i+1))));

		constraints.add(or(sort2.get(range-1)));
		constraints.add(or(sort2.get(range-2)));
		
		return null;// new TwoHotNumber(literals, thermo);
	}
	
	public static <Var> List<Literal<Var>> createLiterals(Function<String, Var> variableGenerator, List<String> names) {
		ArrayList<Literal<Var>> result = new ArrayList<Literal<Var>>();
		for(String name : names)
			result.add(literal(variableGenerator.apply(name)));
		return result;
	}

	public static <Var> List<Literal<Var>> createLiterals(Function<String, Var> literalGenerator, String baseName, int range) {
		return createLiterals(literalGenerator, createLiteralNames(baseName, range));
	}

	public static <Var> List<String> createLiteralNames(String name, int range) {
		List<String> names = new ArrayList<String>();
		
		for(int i=0;i<range;i++)
			names.add(name+i);
		return names;
	}
	

	public static <Var> List<CnfClause<Var>> selectAnd(Function2<Var, String, Var> subvariableGenerator, Literal<Var> result, Literal<Var>[] vars, TwoHotRange<Var> code) {
		List<CnfClause<Var>> conditions = new ArrayList<CnfClause<Var>>();
		
		if(code.size() != vars.length)
			throw new RuntimeException("Lengths do not match: code="+code.size()+", vars="+vars.length);

		List<Literal<Var>> preResult = new ArrayList<Literal<Var>>();
		for(int i=0;i<vars.length;i++)
			preResult.add(literal(subvariableGenerator.apply(result.getVariable(), (result.getNegation()?"i":"")+ "_sv"+i)));
		
		for(int i=0;i<vars.length;i++)
		{
			Literal<Var> res = preResult.get(i);
			Literal<Var> sel = code.get(i);
			Literal<Var> var = vars[i];
			conditions.add(or(not(res), not(sel), var));
			conditions.add(or(res, sel));
			conditions.add(or(res, not(var)));

			conditions.add(or(not(result), res));
		}
		CnfClause<Var> resTrue = new CnfClause<Var>();
		resTrue.add(result);
		for(int i=0;i<vars.length;i++)
			resTrue.add(not(preResult.get(i)));
		conditions.add(resTrue);
		
		return conditions;
	}
	
	public static <Var> BooleanFormula<Var> selectAnd(List<BooleanFormula<Var>> vars, TwoHotRange<Var> number) {
		throw new RuntimeException("incorrect");
		/*
		List<FreeVariable> params = new ArrayList<FreeVariable>();
		CnfLiteral []literals = new CnfLiteral[vars.length];
		
		for(int i=0;i<vars.length;i++)
		{
			FreeVariable var = new FV("param"+i);
			params.add(var);
			literals[i] = new CnfLiteral(var);
		}
		
		List<CnfClause> result = selectAnd(CnfLiteral.One, literals, number);
		
		Cnf<Var> cnf = new Cnf<Var>(result);
		BooleanFormula res = BooleanReplacer.replace(cnf, params, Arrays.asList(vars));
		return res;*/
	}
}
