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

import static org.workcraft.plugins.cpog.optimisation.CnfOperations.not;
import static org.workcraft.plugins.cpog.optimisation.CnfOperations.or;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.util.Function;

public class CnfSorter {
	public static <Var> Cnf<Var> sortRound(Function<String, Var> variableGenerator, List<Literal<Var>> result, List<Literal<Var>> x, List<String> names)
	{
		if(x.size() != result.size())
			throw new RuntimeException("sizes do not match");
		
		List<Literal<Var>> s = new ArrayList<Literal<Var>>();
		for(String name : names)
			s.add(new Literal<Var>(variableGenerator.apply(name+"_th")));
		
		return sortRound(result, s, x);
	}

	private static <Var> Cnf<Var> makeThermometer(List<Literal<Var>> s, List<Literal<Var>> x) {
		// s[i] = s[i-1] + x[i]
		// (!s[i] + s[i-1] + x[i]) (!s[i-1] + s[i]) (!x[i] + s[i])
		Cnf<Var> result = new Cnf<Var>();
		
		result.add(or(not(s.get(0)), x.get(0)));
		result.add(or(not(x.get(0)), s.get(0)));
		for(int i=1;i<s.size();i++)
		{
			result.add(or(not(s.get(i)), s.get(i-1), x.get(i)));
			result.add(or(not(s.get(i-1)), s.get(i)));
			result.add(or(not(x.get(i)), s.get(i)));
		}
		
		return result;
	}

	public static <Var> Cnf<Var> sortRound(List<Literal<Var>> result, List<Literal<Var>> s, List<Literal<Var>> x) {
		
		List<CnfClause<Var>> clauses = new ArrayList<CnfClause<Var>>();
		
		Cnf<Var> thermoCnf = makeThermometer(s, x);
		clauses.addAll(thermoCnf.getClauses());
		
		// y[x] = s[i] x[i+1]
		// (!y[i] + s[i]) (!y[i] + x[i+1]) (!s[i] + !x[i+1] + y[i])
		for(int i=0;i<x.size()-1;i++)
		{
			clauses.add(or(not(result.get(i)), s.get(i)));
			clauses.add(or(not(result.get(i)), x.get(i+1)));
			clauses.add(or(not(s.get(i)), not(x.get(i+1)), result.get(i)));
		}
		
		int n = x.size()-1;
		clauses.add(or(not(result.get(n)), s.get(n)));
		clauses.add(or(not(s.get(n)), result.get(n)));
		
		return new Cnf<Var>(clauses);
	}
}
