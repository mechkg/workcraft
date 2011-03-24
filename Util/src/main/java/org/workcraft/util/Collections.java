package org.workcraft.util;

import java.util.ArrayList;
import java.util.Collection;

public class Collections {
	public static <A,B> Function<Collection<? extends A>, ArrayList<B>> mapWith(final Function<A, B> func) {
		return new Function<Collection<? extends A>, ArrayList<B>>() {
			@Override
			public ArrayList<B> apply(Collection<? extends A> argument) {
				ArrayList<B> result = new ArrayList<B>();
				for(A a : argument)
					result.add(func.apply(a));
				return result;
			}
		};
	}
}
