package org.workcraft.plugins.cpog.optimisation;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.util.Function;

import static org.workcraft.plugins.cpog.optimisation.CnfOperations.*;

class CnfGeneratingOneHotNumberProvider<Var> implements NumberProvider<Var, OneHotIntBooleanFormula<Var>>
{
	private final List<CnfClause<Var>> rho = new ArrayList<CnfClause<Var>>();
	private final Function<String, Var> variableGenerator;

	public CnfGeneratingOneHotNumberProvider(Function<String, Var> variableGenerator) {
		this.variableGenerator = variableGenerator;
	}
	
	public OneHotIntBooleanFormula<Var> generate(String varPrefix, int range) {
		List<Var> vars = new ArrayList<Var>();
		for(int i=0;i<range;i++)
			vars.add(variableGenerator.apply(varPrefix + "sel"+i));

		List<Literal<Var>> literals = new ArrayList<Literal<Var>>();
		
		boolean useSorting = true;
		
		if(!useSorting)
		{
			for(int i=0;i<range;i++)
				for(int j=i+1;j<range;j++)
					rho.add(or(not(vars.get(i)), not(vars.get(j))));
		
			rho.add(or(vars));
		}
		else
		{
			List<String> sortedNames = new ArrayList<String>();
			List<Literal<Var>> sorted = new ArrayList<Literal<Var>>();
			for(int i=0;i<range;i++)
			{
				literals.add(new Literal<Var>(vars.get(i)));
				String name = varPrefix + "sorted"+i;
				sortedNames.add(name);
				sorted.add(new Literal<Var>(variableGenerator.apply(name)));
			}

			Cnf<Var> sorting = CnfSorter.sortRound(variableGenerator, sorted, literals, sortedNames);

			for(int i=0;i<range-1;i++)
				rho.add(or(not(sorted.get(i))));
			rho.add(or(sorted.get(range-1)));
			rho.addAll(sorting.getClauses());
		}
		
		return new OneHotIntBooleanFormula<Var>(vars);
	}

	public static <Var> List<CnfClause<Var>> select(List<Literal<Var>> vars, OneHotIntBooleanFormula<Var> number, boolean inverse) {
		List<CnfClause<Var>> conditions = new ArrayList<CnfClause<Var>>();
		
		if(number.getRange() != vars.size())
			throw new RuntimeException("Lengths do not match");

		for(int i=0;i<vars.size();i++)
			conditions.add(or(not(number.get(i)), inverse?not(vars.get(i)):vars.get(i)));
		
		return conditions;
	}

	public static <Var> List<CnfClause<Var>> select(Literal<Var> result, List<Literal<Var>> vars, OneHotIntBooleanFormula<Var> code) {
		List<CnfClause<Var>> conditions = new ArrayList<CnfClause<Var>>();
		
		if(code.getRange() != vars.size())
			throw new RuntimeException("Lengths do not match");

		Literal<Var> notResult = not(result);
		for(int i=0;i<vars.size();i++)
		{
			conditions.add(or(notResult, not(code.get(i)), vars.get(i)));
			conditions.add(or(result, not(code.get(i)), not(vars.get(i))));
		}
		
		return conditions;
	}

	public List<CnfClause<Var>> getConstraintClauses() {
		return rho;
	}

	public BooleanFormula<Var> less(OneHotIntBooleanFormula<Var> a, OneHotIntBooleanFormula<Var> b) {
		return One.instance();
	}

	@Override
	public BooleanFormula<Var> select(List<BooleanFormula<Var>> vars, OneHotIntBooleanFormula<Var> number) {
		List<Var> params = new ArrayList<Var>();
		List<Literal<Var>> literals = new ArrayList<Literal<Var>>();
		
		for(int i=0;i<vars.size();i++)
		{
			Var var = variableGenerator.apply("param"+i);
			params.add(var);
			literals.add(literal(var));
		}
		
		List<CnfClause<Var>> result = select(literals, number, false);
		
		Cnf<Var> cnf = new Cnf<Var>(result);
		BooleanFormula<Var> res = BooleanReplacer.replace(BooleanOperations.worker, cnf, params, vars);
		return res;
	}

	@Override
	public BooleanFormula<Var> getConstraints() {
		return BooleanOperations.and(getConstraintClauses());
	}
	
}
