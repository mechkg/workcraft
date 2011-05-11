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

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.RecursiveBooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.VariableReplacer;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.Variable;
import org.workcraft.util.Function;


public class LimBooleCnfGenerator<Var> implements RawCnfGenerator<Var, BooleanFormula<Var>> {

	private static final String limboolePath = "C:\\Cygwin\\bin\\limboole.exe";
	
	class VariableCollector extends RecursiveBooleanVisitor<Var, Object>
	{
		int cc = 0;
		
		public final Map<Var, String> names = new HashMap<Var, String>();
		public final Map<String, Var> vars = new HashMap<String, Var>();
		@Override
		public Object visit(Variable<Var> variable) {
			Var var = variable.variable();
			if(!names.containsKey(var)) {
				String name = "v" + cc++;
				vars.put(name, var);
				names.put(var, name);
			}
			
			return null;
		}
	}
	
	@Override
	public CnfTask<Var> getCnf(BooleanFormula<Var> formula) 
	{
		final VariableCollector collector = new VariableCollector();
		formula.accept(collector);
		Map<String, Var> vars = collector.vars;
		Function<Var, String> labelProvider = new Function<Var, String>(){
			@Override
			public String apply(Var argument) {
				return collector.names.get(argument);
			}
		};
		return new CnfTask<Var>(ProcessIO.runViaStreams(FormulaToString.toString(VariableReplacer.replace(labelProvider, BooleanOperations.not(formula)))+"|0|!1", new String[]{limboolePath, "-d"}), vars);
	}
}
