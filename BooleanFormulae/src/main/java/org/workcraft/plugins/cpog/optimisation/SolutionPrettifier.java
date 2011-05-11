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

import org.workcraft.plugins.cpog.optimisation.booleanvisitors.SolutionSubstitutor;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.DumbBooleanWorker;
import org.workcraft.util.Function;


public class SolutionPrettifier 
{
	public static <Var, T> CpogEncoding<Var> prettifySolution(CpogOptimisationTask<Var, T> task, BooleanSolution<Var> solution)
	{
		if(solution==null)
			return null;
		
		List<List<BooleanFormula<Var>>> encodingVars = task.getEncodingVars();
		List<BooleanFormula<Var>> functionVars = task.getFunctionVars();
		if(functionVars == null)
			throw new RuntimeException("functionVars is null");
		if(encodingVars == null)
			throw new RuntimeException("encodingVars is null");
		
		Function<BooleanFormula<Var>, BooleanFormula<Var>> substitutor = SolutionSubstitutor.createSubstitutor(DumbBooleanWorker.instance(), solution);
		BooleanVisitor<Var, Boolean> evaluator = new BooleanEvaluator<Var>();
		
		List<BooleanFormula<Var>> functions = new ArrayList<BooleanFormula<Var>>();
		
		for(int i=0;i<functionVars.size();i++)
			functions.add(substitutor.apply(functionVars.get(i)));

		boolean[][] encoding = new boolean[encodingVars.size()][];
		for(int i=0;i<encodingVars.size();i++)
		{
			encoding[i] = new boolean[encodingVars.get(i).size()];
			for(int j=0;j<encodingVars.get(i).size();j++)
				encoding[i][j] = evaluator.apply(substitutor.apply(encodingVars.get(i).get(j)));
		}
		
		return new CpogEncoding<Var>(encoding, functions);
	}
}
