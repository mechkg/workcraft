package org.workcraft.util;

import java.util.ArrayList;
import java.util.Collection;


public interface Maybe<T> {
	public class Util {
		public static final <T> Function2<Maybe<? extends T>, Maybe<? extends T>, Maybe<T>> first() { 
			return new Function2<Maybe<? extends T>, Maybe<? extends T>, Maybe<T>>() {
				@Override
				public Maybe<T> apply(Maybe<? extends T> argument1, Maybe<? extends T> argument2) {
					return first(argument1, argument2);
				}
			};
		}

		public static <T> Maybe<T> just(final T value) {
			return new Maybe<T>(){
				@Override
				public <R> R accept(MaybeVisitor<? super T, ? extends R> visitor) {
					return visitor.visitJust(value);
				}
			};
		}
		
		public static <T> Maybe<T> nothing() {
			return new Maybe<T>(){
				@Override
				public <R> R accept(MaybeVisitor<? super T, ? extends R> visitor) {
					return visitor.visitNothing();
				}
			};
		}
		
		public static <R> Function<R,Maybe<R>> just() { return  new Function<R, Maybe<R>>() {
				@Override
				public Maybe<R> apply(R argument) {
					return just(argument);
				}
			};
		}
		
		public static <A, B> Maybe<B> applyFunc(Maybe<? extends A> a, final Function<? super A, ? extends B> func) {
			return a.accept(new MaybeVisitor<A,Maybe<B>>() {

				@Override
				public Maybe<B> visitJust(A a) {
					B res = func.apply(a);
					return just(res);
				}

				@Override
				public Maybe<B> visitNothing() {
					return nothing();
				}
			});
		}
		
		public static <T> T orElse(Maybe<? extends T> maybe, final T fallBack) {
			return maybe.accept(new MaybeVisitor<T, T>() {

				@Override
				public T visitJust(T just) {
					return just;
				}

				@Override
				public T visitNothing() {
					return fallBack;
				}
			});
		};
		
		public static <T> T orElseDo(Maybe<? extends T> maybe, final Function0<T> fallBack) {
			return maybe.accept(new MaybeVisitor<T, T>() {

				@Override
				public T visitJust(T just) {
					return just;
				}

				@Override
				public T visitNothing() {
					return fallBack.apply();
				}
			});
		};
		
		public static <T> void doIfJust(final Maybe<? extends T> maybe, final Action1<? super T> action) {
			maybe.accept(new MaybeVisitor<T, Nothing>() {

				@Override
				public Nothing visitJust(T just) {
					action.run(just);
					return Nothing.VALUE;
				}

				@Override
				public Nothing visitNothing() {
					return Nothing.VALUE;
				}
			});
		}

		public static <T> Function<? super Collection<? extends Maybe<? extends T>>, Collection<T>> filterJust() {
			return new Function<Collection<? extends Maybe<? extends T>>, Collection<T>>() {

				@Override
				public Collection<T> apply(Collection<? extends Maybe<? extends T>> argument) {
					final ArrayList<T> result = new ArrayList<T>();
					for(Maybe<? extends T> t : argument) {
						doIfJust(t, new Action1<T>(){

							@Override
							public void run(T argument) {
								result.add(argument);
							}
						});
					}
					return result;
				}
			};
		}

		public static <A> Maybe<A> first(final Maybe<? extends A> m1, final Maybe<? extends A> m2) {
			return m1.accept(new MaybeVisitor<A, Maybe<A>>() {
				@Override
				public Maybe<A> visitJust(A a) {
					return just(a);
				}
				@Override
				public Maybe<A> visitNothing() {
					return m2.accept(new MaybeVisitor<A, Maybe<A>>() {

						@Override
						public Maybe<A> visitJust(A a) {
							return just(a);
						}

						@Override
						public Maybe<A> visitNothing() {
							return nothing();
						}
					});
				}
			});
		}

		public static <A,B> Function<Maybe<? extends A>, Maybe<B>> lift(final Function<? super A, ? extends B> transform) {
			return new Function<Maybe<? extends A>, Maybe<B>>(){
				@Override
				public Maybe<B> apply(Maybe<? extends A> argument) {
					return applyFunc(argument, transform);
				}
			};
		}

		public static <A,B> Function<Function<? super A, ? extends B>, Function<Maybe<? extends A>, Maybe<B>>> lift() {
			return new Function<Function<? super A,? extends B>, Function<Maybe<? extends A>,Maybe<B>>>(){
				@Override
				public Function<Maybe<? extends A>, Maybe<B>> apply(Function<? super A, ? extends B> argument) {
					Function<Maybe<? extends A>, Maybe<B>> res = Maybe.Util.<A,B>lift(argument);
					return res;
				}
			};
		}
		
		public static class NothingFound extends Exception {
			private static final long serialVersionUID = 1L;
			
		}
		
		interface ThrowingEvaluator<A, E extends Exception> {
			A evaluate() throws E;
		}
		
		public static <A> A extract(Maybe<A> maybe) throws NothingFound {
			return maybe.accept(new MaybeVisitor<A, ThrowingEvaluator<A, NothingFound>>() {

				@Override
				public ThrowingEvaluator<A, NothingFound> visitJust(final A just) {
					return new ThrowingEvaluator<A, NothingFound>() {
						@Override
						public A evaluate() throws NothingFound {
							return just;
						}
					};
				}

				@Override
				public ThrowingEvaluator<A, NothingFound> visitNothing() {
					return new ThrowingEvaluator<A, NothingFound>(){
						@Override
						public A evaluate() throws NothingFound {
							throw new NothingFound();
						}
					};
				}
			}).evaluate();
		}
		
		public static <A> A toNullable(Maybe<A> maybe) {
			return maybe.accept(new MaybeVisitor<A, A>() {
				@Override
				public A visitJust(A value) {
					if(value != null)
						return value;
					else
						throw new NullPointerException();
				}

				@Override
				public A visitNothing() {
					return null;
				}
			});
		}

		public static <A> Maybe<A> fromNullable(A nullable) {
			Maybe<A> nothing = nothing();
			return nullable == null ? nothing : just(nullable);
		}
		
		public static <A,B> Maybe<B> bind(Maybe<A> a, final Function<A, Maybe<B>> f) {
			return a.accept(new MaybeVisitor<A, Maybe<B>>() {

				@Override
				public Maybe<B> visitJust(A just) {
					return f.apply(just);
				}

				@Override
				public Maybe<B> visitNothing() {
					return nothing();
				}
			});
		}

		public static <A> boolean isNothing(Maybe<A> m) {
			return !isJust(m);
		}
		
		public static <A> boolean isJust(Maybe<A> m) {
			return m.accept(new MaybeVisitor<A, Boolean>() {

				@Override
				public Boolean visitJust(A just) {
					return true;
				}

				@Override
				public Boolean visitNothing() {
					return false;
				}
			});
		}
	}
	
	public <R> R accept(MaybeVisitor<? super T, ? extends R> visitor);
	
}
