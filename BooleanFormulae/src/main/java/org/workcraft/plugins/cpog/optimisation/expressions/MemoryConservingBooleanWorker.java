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

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;


class Inverter<Var> extends BooleanVisitor<Var, BooleanFormula<Var>> {
	@SuppressWarnings("rawtypes")
	private final static Inverter instance = new Inverter();
	
	@SuppressWarnings("unchecked")
	static <Var> Inverter<Var> instance() {
		return instance;
	}

	@Override
	public BooleanFormula<Var> visit(Not<Var> node) {
		return node.getX();
	}

	protected BooleanFormula<Var> visitDefault(BooleanFormula<Var> node) {
		return Not.create(node);
	}

	@Override
	public BooleanFormula<Var> visit(And<Var> node) {
		return visitDefault(node);
	}

	@Override
	public BooleanFormula<Var> visit(Iff<Var> node) {
		return visitDefault(node);
	}

	@Override
	public BooleanFormula<Var> visit(Xor<Var> node) {
		return visitDefault(node);
	}

	@Override
	public BooleanFormula<Var> visit(Zero<Var> node) {
		throw new RuntimeException("no constants expected here");
	}

	@Override
	public BooleanFormula<Var> visit(One<Var> node) {
		throw new RuntimeException("no constants expected here");
	}

	@Override
	public BooleanFormula<Var> visit(Imply<Var> node) {
		return visitDefault(node);
	}

	@Override
	public BooleanFormula<Var> visit(Variable<Var> var) {
		return visitDefault(var);
	}

	@Override
	public BooleanFormula<Var> visit(Or<Var> node) {
		return visitDefault(node);
	}
}

public class MemoryConservingBooleanWorker implements ReducedBooleanWorker {
	Map<BooleanFormula<?>, Integer> codes = new HashMap<BooleanFormula<?>, Integer>();
	Map<IntPair, BooleanFormula<?>> ands = new HashMap<IntPair, BooleanFormula<?>>();
	Map<IntPair, BooleanFormula<?>> iffs = new HashMap<IntPair, BooleanFormula<?>>();
	Map<Integer, BooleanFormula<?>> nots = new HashMap<Integer, BooleanFormula<?>>();
	int nextCode = 0;

	@SuppressWarnings("unchecked")
	<K, V> BooleanFormula<V> unsafeGet(Map<K, BooleanFormula<?>> map, K key) {
		return (BooleanFormula<V>) map.get(key);
	}
	
	Integer getCode(BooleanFormula<?> f) {
		Integer code = codes.get(f);
		if (code == null) {
			// if(!(f instanceof FreeVariable))
			// System.out.println("warning: unknown code for formula f=[" +
			// f.getClass().getSimpleName()+"] " + formulaToStr(f));
			code = newCode(f);
		}
		return code;
	}

	private Integer newCode(BooleanFormula<?> f) {
		if (codes.containsKey(f))
			throw new RuntimeException("Code already exists for formula: ");
		int code = nextCode++;
		codes.put(f, code);
		return code;
	}

	class IntPair {
		public IntPair(Integer x, Integer y) {
			if (y < x) {
				Integer tmp = x;
				x = y;
				y = tmp;
			}
			this.x = x;
			this.y = y;
		}

		final Integer x;
		final Integer y;

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj == null)
				return false;
			IntPair other = (IntPair) obj;
			if (!other.x.equals(x))
				return false;
			if (!other.y.equals(y))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			return x * 1037 + y;
		}
	}

	public <Var> BooleanFormula<Var> not(BooleanFormula<Var> f) {
		Integer code = getCode(f);
		BooleanFormula<Var> res = unsafeGet(nots, code);
		if (res == null) {
			res = f.accept(Inverter.<Var>instance());
			ensureHaveCode(res);
			nots.put(code, res);
		}
		return res;
	}

	public <Var> BooleanFormula<Var> and(BooleanFormula<Var> x, BooleanFormula<Var> y) {
		IntPair pair = getCodePair(x, y);
		BooleanFormula<Var> result = unsafeGet(ands, pair);
		if (result == null) {
			result = And.create(x, y);
			ands.put(pair, result);
			newCode(result);
		}
		return result;
	}

	private IntPair getCodePair(BooleanFormula<?> x, BooleanFormula<?> y) {
		Integer xCode = getCode(x);
		Integer yCode = getCode(y);
		IntPair pair = new IntPair(xCode, yCode);
		return pair;
	}

	public <Var> BooleanFormula<Var> iff(BooleanFormula<Var> x, BooleanFormula<Var> y) {
		IntPair pair = getCodePair(x, y);
		IntPair pairInv = getCodePair(not(x), not(y));
		if (pair.x > pairInv.x) {
			pair = pairInv;
			x = not(x);
			y = not(y);
		}
		BooleanFormula<Var> result = unsafeGet(iffs, pair);
		if (result == null) {
			result = Iff.create(x, y);
			iffs.put(pair, result);
			newCode(result);
		}
		return result;
	}

	private void ensureHaveCode(BooleanFormula<?> formula) {
		if (!codes.containsKey(formula))
			newCode(formula);
	}
}
