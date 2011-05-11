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
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

public class BinaryNumberProvider implements
		NumberProvider<FreeVariable, BinaryIntBooleanFormula<FreeVariable>> {

	@Test
	public void testBigConstraint()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		p.generate("x", 25);
		Assert.assertEquals("(!xb4|(!xb3|(!xb2&(!xb1&!xb0))))", FormulaToString.printWithToString(p.getConstraints()));
	}


	@Test
	public void testValuesCount()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		BinaryIntBooleanFormula<FreeVariable> num = p.generate("", 9);
		Assert.assertEquals(9, num.getValuesCount());
	}
	@Test
	public void testBigSelect()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		BinaryIntBooleanFormula<FreeVariable> num = p.generate("", 9);
		ArrayList<BooleanFormula<FreeVariable>> f = new ArrayList<BooleanFormula<FreeVariable>>();
		f.add(var(new FreeVariable("a")));
		f.add(var(new FreeVariable("b")));
		f.add(var(new FreeVariable("c")));
		f.add(var(new FreeVariable("d")));
		f.add(var(new FreeVariable("e")));
		f.add(var(new FreeVariable("f")));
		f.add(var(new FreeVariable("g")));
		f.add(var(new FreeVariable("h")));
		f.add(var(new FreeVariable("i")));
		BooleanFormula<FreeVariable> result = p.select(f, num);
//		System.out.println(result.accept(new FormulaToString()));
		Assert.assertEquals("((b3&((b2&0)|(!b2&((b1&0)|(!b1&((b0&0)|(!b0&i)))))))|(!b3&((b2&((b1&((b0&h)|(!b0&g)))|(!b1&((b0&f)|(!b0&e)))))|(!b2&((b1&((b0&d)|(!b0&c)))|(!b1&((b0&b)|(!b0&a))))))))", FormulaToString.printWithToString(result));
	}

	@Test
	public void testEmptyConstraint()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		p.generate("x", 2);
		Assert.assertEquals("1", FormulaToString.printWithToString(p.getConstraints()));
	}
	
	@Test
	public void testZeroBitEmptyConstraint()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		p.generate("", 1);
		Assert.assertEquals("1", FormulaToString.printWithToString(p.getConstraints()));
	}
	
	@Test
	public void testSelectZeroBit()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		BinaryIntBooleanFormula<FreeVariable> num = p.generate("", 1);
		ArrayList<BooleanFormula<FreeVariable>> f = new ArrayList<BooleanFormula<FreeVariable>>();
		f.add(var(new FreeVariable("x")));
		BooleanFormula<FreeVariable> result = p.select(f, num);
		Assert.assertEquals("x", FormulaToString.printWithToString(result));
	}
	
	@Test
	public void testSelectOneBit()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		BinaryIntBooleanFormula<FreeVariable> num = p.generate("", 2);
		ArrayList<BooleanFormula<FreeVariable>> f = new ArrayList<BooleanFormula<FreeVariable>>();
		f.add(var(new FreeVariable("x")));
		f.add(var(new FreeVariable("y")));
		BooleanFormula<FreeVariable> result = p.select(f, num);
		Assert.assertEquals("((b0&y)|(!b0&x))", FormulaToString.printWithToString(result));
	}
	
	@Test
	public void testSelectThreeValues()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		BinaryIntBooleanFormula<FreeVariable> num = p.generate("", 3);
		List<BooleanFormula<FreeVariable>> f = new ArrayList<BooleanFormula<FreeVariable>>();
		f.add(var(new FreeVariable("x")));
		f.add(var(new FreeVariable("y")));
		f.add(var(new FreeVariable("z")));
		BooleanFormula<FreeVariable> result = p.select(f, num);
		Assert.assertEquals("((b1&((b0&0)|(!b0&z)))|(!b1&((b0&y)|(!b0&x))))", FormulaToString.printWithToString(result));
	}
	
	List<BooleanFormula<FreeVariable>> constraints = new ArrayList<BooleanFormula<FreeVariable>>(); 
	
	@Override
	public BinaryIntBooleanFormula<FreeVariable> generate(String varPrefix, int range) {
		if(range == 0)
			throw new RuntimeException("range=0");
		int varCount = 0;
		int tmp = range-1;
		while(tmp>0)
		{
			tmp/=2;
			varCount++;
		}
		
		List<FreeVariable> vars = new ArrayList<FreeVariable>();
		for(int i=0;i<varCount;i++)
			vars.add(new FreeVariable(varPrefix + "b"+i));

		if(1<<varCount != range)
			constraints.add(less(vars, varCount-1, range));
		
		return new BinaryIntBooleanFormula<FreeVariable>(vars, range);
	}

	private BooleanFormula<FreeVariable> less(List<FreeVariable> a, int n, int b) {
		FreeVariable an = a.get(n);
		boolean bn = ((b>>n)&1) > 0;
		BooleanFormula<FreeVariable> nan = not(BooleanOperations.<FreeVariable>var(an));
		
		if(n==0)
			if(bn)
				return nan;
			else
				return Zero.instance();
		
		BooleanFormula<FreeVariable> L = less(a, n-1, b);

		if(bn)
			return or(nan, L);
		else
			return and(nan, L);
	}

	@Override
	public BooleanFormula<FreeVariable> getConstraints() {
		return and(constraints);
	}

	@Override
	public BooleanFormula<FreeVariable> select(List<BooleanFormula<FreeVariable>> vars, BinaryIntBooleanFormula<FreeVariable> number) {
		List<FreeVariable> bits = number.getVars();
		if(number.getValuesCount() != vars.size())
			throw new RuntimeException("lengths do not match: vars=" + vars.size() + ", number="+number.getValuesCount());
		return select(vars, bits, bits.size(), 0, number.getValuesCount());
	}

	private BooleanFormula<FreeVariable> select(List<BooleanFormula<FreeVariable>> vars, List<FreeVariable> bits, int length, int offset, int threshold) {
		if(offset >= threshold)
			return Zero.instance();
		if(length == 0)
			return vars.get(offset);
		BooleanFormula<FreeVariable> x = var(bits.get(length-1));
		BooleanFormula<FreeVariable> nx = not(x);
		return or(
				and(x, select(vars, bits, length-1, offset+(1<<(length-1)), threshold)),
				and(nx, select(vars, bits, length-1, offset, threshold))
		);
	}

	@Override
	public BooleanFormula<FreeVariable> less(BinaryIntBooleanFormula<FreeVariable> a,
			BinaryIntBooleanFormula<FreeVariable> b) {
		return less(a.getVars(), b.getVars(), a.getVars().size()-1);
	}

	private BooleanFormula<FreeVariable> less(List<FreeVariable> a, List<FreeVariable> b, int n) {
		FreeVariable an = a.get(n);
		BooleanFormula<FreeVariable> bn = var(b.get(n));
		BooleanFormula<FreeVariable> nan = not(var(an));
		
		if(n==0)
			return and(nan, bn);
		
		BooleanFormula<FreeVariable> L = less(a, b, n-1);

		return or(and(nan, bn), and(or(nan, bn), L));
	}
}
