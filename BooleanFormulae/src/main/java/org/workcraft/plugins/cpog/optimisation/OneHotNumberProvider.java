package org.workcraft.plugins.cpog.optimisation;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.util.Function;

import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.*;

public class OneHotNumberProvider<Var> implements NumberProvider<Var, OneHotIntBooleanFormula<Var>>
{
	private final List<BooleanFormula<Var>> rho = new ArrayList<BooleanFormula<Var>>();
	private final Function<String, Var> variableGenerator;

	public OneHotNumberProvider(Function<String, Var> variableGenerator)
	{
		this.variableGenerator = variableGenerator;
	}
	
	@Override
	public OneHotIntBooleanFormula<Var> generate(String varPrefix, int range) {
		List<BooleanFormula<Var>> varBFs = new ArrayList<BooleanFormula<Var>>();
		List<Var> vars = new ArrayList<Var>();
		for(int i=0;i<range;i++) {
			Var var = variableGenerator.apply(varPrefix + "sel"+i);
			vars.add(var);
			varBFs.add(var(var));
		}

		for(int i=0;i<range;i++)
			for(int j=i+1;j<range;j++)
				rho.add(or(not(varBFs.get(i)), not(varBFs.get(j))));
		
		rho.add(or(varBFs));
		
		return new OneHotIntBooleanFormula<Var>(vars);
	}

	@Override
	public BooleanFormula<Var> select(List<BooleanFormula<Var>> booleanFormulas, OneHotIntBooleanFormula<Var> number) {
		List<BooleanFormula<Var>> conditions = new ArrayList<BooleanFormula<Var>>();
		
		if(number.getRange() != booleanFormulas.size())
			throw new RuntimeException("Lengths do not match");

		for(int i=0;i<booleanFormulas.size();i++)
			conditions.add(imply(var(number.get(i)), booleanFormulas.get(i)));
		
		return and(conditions);
	}

	@Override
	public BooleanFormula<Var> getConstraints() {
		return and(rho);
	}

	@Override
	public BooleanFormula<Var> less(OneHotIntBooleanFormula<Var> a, OneHotIntBooleanFormula<Var> b) {
		return One.instance();
	}
}
