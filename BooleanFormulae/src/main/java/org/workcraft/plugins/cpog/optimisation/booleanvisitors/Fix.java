package org.workcraft.plugins.cpog.optimisation.booleanvisitors;

import org.workcraft.util.Function;
import org.workcraft.util.Function2;

import static org.workcraft.util.Function.Util.*;

public class Fix {

	private final static class MutableFunction<A, B> implements Function<A, B> {
		public Function<A, B> f;
		
		@Override
		public B apply(A argument) {
			return f.apply(argument);
		}
	}
	
	/**
	 * Computes the fixed point of function f.
	 * Only terminates successfully if f is non-strict (that is returns without calling its argument).
	 */
	public static <A, B, R extends Function<A,B>> R fix(final Function<? super Function<A, B>, ? extends R> f) {
		MutableFunction<A, B> mutable = new MutableFunction<A, B>();
		R result = f.apply(mutable);
		mutable.f = result;
		return result;
	}

	public static <A, B> Function<A, B> fix(final Function2<Function<A, B>, A, B> f) {
		return fix(curry(f));
	}
	
	public interface Endo<T> extends Function<T,T> {}
}
