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
		
		public static <A, R> Function<A, R> constant(final R r) {
			return new Function<A, R>(){
				@Override
				public R apply(A argument) {
					return r;
				}
			};
		}
		
		public static <A, B, C> Function<A, C> composition(final Function<? super A, ? extends B> f1, final Function<? super B, ? extends C> f2) {
			return new Function<A, C>() {
				@Override
				public C apply(A argument) {
					return f2.apply(f1.apply(argument));
				}
			};
		}
		
		public static <A, B, C, D> Function<A, D> composition(final Function<A, B> f1, final Function<B, C> f2, final Function<C, D> f3) {
			return composition(composition(f1, f2), f3);
		}
		
		public static <A, B, C, D, E> Function<A, E> composition(final Function<A, B> f1, final Function<B, C> f2, final Function<C, D> f3, final Function<D, E> f4) {
			return composition(composition(f1, f2, f3), f4);
		}
    }
}
