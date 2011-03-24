package org.workcraft.util;

public interface Function<A,R> {
    public R apply(A argument);
    
    public class Util {
    	public static <A,B,R> Function<B, R> applyPartial(final Function2<A, B, R> f, final A a) {
			return new Function<B, R>(){
				@Override
				public R apply(B b) {
					return f.apply(a,b);
				}
			};
    	}
    	
    	public static <A,B,R> Function<Function2<A,B,R>, Function<A, Function<B, R>>> curry() {
			return new Function<Function2<A,B,R>, Function<A,Function<B,R>>>(){
				@Override
				public Function<A, Function<B, R>> apply(final Function2<A, B, R> f) {
					return curry(f);
				}
			};
    	}
    	
		public static <A, B, R> Function<A, Function<B, R>> curry(final Function2<A, B, R> f) {
			return new Function<A, Function<B,R>>(){
				@Override
				public Function<B, R> apply(A a) {
					return applyPartial(f, a);
				}
			};
		}
    }
}
