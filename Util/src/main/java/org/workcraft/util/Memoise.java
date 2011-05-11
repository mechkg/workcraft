package org.workcraft.util;

import java.util.HashMap;

public class Memoise {

	public static <A, B> Function<A, B> memoise(final Function<A, B> f) {
		final HashMap<A, B> cache = new HashMap<A, B>();
		return new Function<A, B>(){
			@Override
			public B apply(A argument) {
				B res = cache.get(argument);
				if(res==null) {
					res = f.apply(argument);
					cache.put(argument, res);
				}
				return res;
			}
		};
	}
}
