package org.workcraft.plugins.cpog.optimisation;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.util.Function;

import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.*;

class ThermometerNumberProvider<Var> implements NumberProvider<Var, ThermometerBooleanFormula<Var>>
{
	private final List<BooleanFormula<Var>> rho = new ArrayList<BooleanFormula<Var>>();
	private final Function<String, Var> variableGenerator;

	public ThermometerNumberProvider(Function<String, Var> variableGenerator)
	{
		this.variableGenerator = variableGenerator;
	}
	
	@Override
	public ThermometerBooleanFormula<Var> generate(String varPrefix, int range) {
		List<Var> vars = new ArrayList<Var>();
		for(int i=0;i<range-1;i++)
			vars.add(variableGenerator.apply(varPrefix + "sel"+i));

		for(int i=0;i<range-2;i++)
			rho.add(imply(var(vars.get(i+1)), var(vars.get(i))));
		
		return new ThermometerBooleanFormula<Var>(vars);
	}

	@Override
	public BooleanFormula<Var> select(List<BooleanFormula<Var>> vars, ThermometerBooleanFormula<Var> number) {
		List<BooleanFormula<Var>> conditions = new ArrayList<BooleanFormula<Var>>();
		
		List<Var> digits = number.getVars();
		int N = digits.size();
		if(N+1 != vars.size())
			throw new RuntimeException("Lengths do not match");
		if(N==0)
			return vars.get(0);

		conditions.add(imply(not(var(digits.get(0))), vars.get(0)));
		conditions.add(imply(var(digits.get(N-1)), vars.get(N)));
		for(int i=0;i<N-1;i++) {
			conditions.add(imply(and(var(digits.get(i)), not(var(digits.get(i+1)))), vars.get(i+1)));
		}
		
		return and(conditions);
	}

	@Override
	public BooleanFormula<Var> getConstraints() {
		return and(rho);
	}

	public BooleanFormula<Var> lessOrEquals(ThermometerBooleanFormula<Var> a, ThermometerBooleanFormula<Var> b) {
		List<BooleanFormula<Var>> conditions = new ArrayList<BooleanFormula<Var>>();
		List<Var> aVars = a.getVars();
		List<Var> bVars = b.getVars();
		for(int i=0;i<aVars.size();i++)
			conditions.add(imply(var(aVars.get(i)), var(bVars.get(i))));
		return and(conditions);
	}
	
	@Override
	public BooleanFormula<Var> less(ThermometerBooleanFormula<Var> a, ThermometerBooleanFormula<Var> b) {
		return not(lessOrEquals(b, a));
	}
}
