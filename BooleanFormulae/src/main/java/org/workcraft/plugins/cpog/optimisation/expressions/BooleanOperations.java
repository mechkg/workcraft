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
package org.workcraft.plugins.cpog.optimisation.expressions;

import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;


public class BooleanOperations 
{
	public static BooleanWorker worker = new BooleanWorkerPrettifier(new MemoryConservingBooleanWorker());
	//private static BooleanWorker worker = new DumbBooleanWorker();
	
	public static <Var> BooleanFormula<Var> not(BooleanFormula<Var> x) {
		return worker.not(x);
	}

	public static <Var> BooleanFormula<Var> createAnd(List<? extends BooleanFormula<Var>> conditions) {
		return createAnd(conditions, 0, conditions.size());
	}

	private static <Var> BooleanFormula<Var> createAnd(List<? extends BooleanFormula<Var>> conditions, int start, int end) {
		int size = end-start;
		if(size == 0)
			return one();
		else
			if(size == 1)
				return conditions.get(start);
			else
			{
				int split = (end+start)/2;
				return and(createAnd(conditions, start, split), createAnd(conditions, split, end));
			}
	}

	public static <Var> BooleanFormula<Var> createOr(List<? extends BooleanFormula<Var>> conditions) {
		return createOr(conditions, 0, conditions.size());
	}

	private static <Var> BooleanFormula<Var> createOr(List<? extends BooleanFormula<Var>> conditions, int start, int end) {
		int size = end-start;
		if(size == 0)
			return zero();
		else
			if(size == 1)
				return conditions.get(start);
			else
			{
				int split = (end+start)/2;
				return or(createOr(conditions, start, split), createOr(conditions, split, end));
			}
	}

	public static <Var> BooleanFormula<Var> and(List<? extends BooleanFormula<Var>> conditions) {
		return createAnd(conditions);
	}

	public static <Var> BooleanFormula<Var> or(BooleanFormula<Var>... conditions) {
		return or(Arrays.asList(conditions));
	}
	
	public static <Var> BooleanFormula<Var> or(List<? extends BooleanFormula<Var>> conditions) {
		return createOr(conditions);
	}

	public static <Var> BooleanFormula<Var> iff(BooleanFormula<Var> x, BooleanFormula<Var> y) {
		return worker.iff(x, y);
	}

	public static <Var> BooleanFormula<Var> xor(BooleanFormula<Var> x, BooleanFormula<Var> y) {
		return worker.xor(x, y);
	}

	public static <Var> BooleanFormula<Var> imply(BooleanFormula<Var> x, BooleanFormula<Var> y) {
		return worker.imply(x, y);
	}

	public static <Var> BooleanFormula<Var> and(BooleanFormula<Var> x, BooleanFormula<Var> y) {
		return worker.and(x, y);
	}

	public static <Var> BooleanFormula<Var> or(BooleanFormula<Var> x, BooleanFormula<Var> y) {
		return worker.or(x, y);
	}
	
	public static <Var> BooleanFormula<Var> one() {
		return worker.one();
	}

	public static <Var> BooleanFormula<Var> zero() {
		return worker.zero();
	}

	public static <Var> BooleanFormula<Var> constant(boolean value) {
		if (value) 
			return one();
		else 
			return zero();
	}
	
	public static <Var> BooleanFormula<Var> var(Var var) {
		return worker.var(var);
	}
}
