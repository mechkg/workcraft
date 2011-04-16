package org.workcraft.dependencymanager.advanced.core;

import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.ExpressionBase.ValueHandleTuple;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.dependencymanager.advanced.user.PickyModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.PickyModifiableExpressionBase;
import org.workcraft.dependencymanager.util.listeners.Listener;
import org.workcraft.util.Collections;
import org.workcraft.util.Function;
import org.workcraft.util.Function0;
import org.workcraft.util.Function2;
import org.workcraft.util.Function3;
import org.workcraft.util.Maybe;
import org.workcraft.util.SubstructureView;
import org.workcraft.util.TwoWayFunction;

import pcollections.PVector;
import pcollections.TreePVector;

public class Expressions {
	public static <T> Expression<T> constant(final T value) {
		return new ExpressionBase<T> () {
			@Override
			public T evaluate(EvaluationContext resolver) {
				return value;
			}
		};
	}
	
	public static Expression<Integer> sum(final Expression<Integer> a, final Expression<Integer> b) {
		return new ExpressionBase<Integer>() {
			@Override
			public Integer evaluate(EvaluationContext resolver) {
				return resolver.resolve(a) + resolver.resolve(b);
			}

		};
	}
	public static Expression<Integer> size(final Expression<? extends Collection<?>> collection) {
		return new ExpressionBase<Integer>() {
			@Override
			public Integer evaluate(EvaluationContext resolver) {
				return resolver.resolve(collection).size();
			}
		};
	}
	
	public static <T2, T1  extends T2>ModifiableExpression<T2> cast(
			final ModifiableExpression<T1> expr,
			final Class<T1> cls,
			final Class<T2> cls2) {
		return new ModifiableExpressionImpl<T2>() {

			@Override
			protected void simpleSetValue(T2 newValue) {
				expr.setValue(cls.cast(newValue));
			}

			@Override
			protected T2 evaluate(EvaluationContext context) {
				return context.resolve(expr);
			}
		};
	}

	public static <T> ModifiableExpression<T> modifiableExpression(final Expression<? extends T> getter, final ModifiableExpression<T> setter) {
		return new ModifiableExpression<T>() {

			@Override
			public ValueHandleTuple<? extends T> getValue(Listener subscriber) {
				return getter.getValue(subscriber);
			}

			@Override
			public void setValue(T newValue) {
				setter.setValue(newValue);
			}
		};
	}
	
	static void notNull(Object... objects) {
		for(Object o : objects) {
			if (o==null)
				throw new NullPointerException();
		}
	}

	public static <A,B> Expression<B> bindFunc(final Expression<? extends A> expr1, final Function<? super A, ? extends B> func) {
		notNull(expr1, func);
		return new ExpressionBase<B>(){
			@Override
			protected B evaluate(EvaluationContext context) {
				B result = func.apply(context.resolve(expr1));
				if(result == null)
					throw new RuntimeException(func + " returned null!");
				return result;
			}
		};
	}

	public static <A,B,C> Expression<C> bindFunc(final Expression<? extends A> expr1, final Expression<? extends B> expr2, final Function2<? super A, ? super B, ? extends C> func) {
		notNull(expr1, expr2, func);
		return new ExpressionBase<C>(){
			@Override
			protected C evaluate(EvaluationContext context) {
				C result = func.apply(context.resolve(expr1),context.resolve(expr2));
				if(result == null)
					throw new RuntimeException(func + " returned null!");
				return result;
			}
		};
	}

	public static <A,B,C,R> Expression<R> bindFunc(final Expression<? extends A> expr1, final Expression<? extends B> expr2, final Expression<? extends C> expr3, final Function3<? super A, ? super B, ? super C, ? extends R> func) {
		notNull(expr1, expr2, expr3, func); 
		return new ExpressionBase<R>(){
			@Override
			protected R evaluate(EvaluationContext context) {
				R result = func.apply(context.resolve(expr1), context.resolve(expr2), context.resolve(expr3));
				if(result == null)
					throw new RuntimeException(func + " returned null!");
				return result;
			}
		};
	}

	public static <A,B> Expression<? extends B> bind_tightly(final Expression<? extends A> expr1, final Function<? super A, ? extends Expression<? extends B>> func) {
		return new ExpressionBase<B>(){
			@Override
			protected B evaluate(EvaluationContext context) {
				return context.resolve(func.apply(context.resolve(expr1)));
			}
		};
	}

	public static <A,B> Expression<B> bind(final Expression<? extends A> expr1, final Function<? super A, ? extends Expression<? extends B>> func) {
		return join(bindFunc(expr1, func));
	}

	public static <A> Expression<A> join(final Expression<? extends Expression<? extends A>> boundFunc) {
		notNull(boundFunc);
		return new ExpressionBase<A>(){
			@Override
			protected A evaluate(EvaluationContext context) {
				Expression<? extends A> res = context.resolve(boundFunc);
				if(res == null)
					throw new RuntimeException(boundFunc + " returned null!");
				return context.resolve(res);
			}
		};
	}
	
	public static <A,B,C> Expression<? extends C> bind(final Expression<? extends A> expr1, final Expression<? extends B> expr2, final Combinator2<? super A, ? super B, ? extends C> func) {
		notNull(expr1, expr2, func);
		return new ExpressionBase<C>(){
			@Override
			protected C evaluate(EvaluationContext context) {
				return context.resolve(func.apply(context.resolve(expr1),context.resolve(expr2)));
			}
		};
	}

	public static <A,B> ModifiableExpression<B> bind(final ModifiableExpression<A> expr1, final ModifiableExpressionCombinator<A, B> combinator) {
		notNull(expr1, combinator); 
		return new ModifiableExpressionBase<B>(){
			@Override
			public void setValue(B newValue) {
				expr1.setValue(combinator.set(newValue));
			}

			@Override
			protected B evaluate(EvaluationContext context) {
				return context.resolve(combinator.get(context.resolve(expr1)));
			}
		};
	}	
	
	public static <A,R> ModifiableExpression<R> applyM(final ModifiableExpression<A> exprA, final Expression<TwoWayFunction<A, R>> func) {
		return new ModifiableExpressionBase<R>() {
			@Override
			public void setValue(R newValue) {
				exprA.setValue(GlobalCache.eval(func).reverse(newValue));
			}

			@Override
			protected R evaluate(EvaluationContext context) {
				return context.resolve(func).apply(context.resolve(exprA));
			}
		};
	}

	public static <A,R> ModifiableExpression<R> bindFunc(final ModifiableExpression<A> exprA, final SubstructureView<A, R> func) {
		return new ModifiableExpressionBase<R>() {
			@Override
			public void setValue(R newValue) {
				exprA.setValue(func.reverse(GlobalCache.eval(exprA), newValue));
			}

			@Override
			protected R evaluate(EvaluationContext context) {
				return func.apply(context.resolve(exprA));
			}
		};
	}

	public static <A,R> ModifiableExpression<R> bind(final ModifiableExpression<A> exprA, final SubstructureView<A, R> func) {
		return new ModifiableExpressionBase<R>() {
			@Override
			public void setValue(R newValue) {
				exprA.setValue(func.reverse(GlobalCache.eval(exprA), newValue));
			}

			@Override
			protected R evaluate(EvaluationContext context) {
				return func.apply(context.resolve(exprA));
			}
		};
	}

	public static <A, B, S> PickyModifiableExpression<B,S> bind(final ModifiableExpression<A> expr1, final PickyModifiableExpressionCombinator<A, B, S> combinator) {
		return new PickyModifiableExpressionBase<B,S>(){

			@Override
			public Maybe<S> setValue(B newValue) {
				return combinator.set(newValue).accept(
						new EitherVisitor<S, A, Maybe<S>>() {

							@Override
							public Maybe<S> visit1(S value) {
								return Maybe.Util.just(value);
							}

							@Override
							public Maybe<S> visit2(A value) {
								expr1.setValue(value);
								return Maybe.Util.nothing();
							}
						}
				);
			}

			@Override
			protected B evaluate(EvaluationContext context) {
				return context.resolve(combinator.get(context.resolve(expr1)));
			}
		};
	}
	
	public static <A,F,R> Expression<? extends R> apply(final Expression<? extends Function<? super A, ? extends R>> func, final Expression<? extends A> arg) {
		return new ExpressionBase<R>(){
			@Override
			protected R evaluate(EvaluationContext context) {
				return context.resolve(func).apply(context.resolve(arg));
			}
		};
	}
	
	public static <A,B> Function<Collection<? extends A>, Expression<PVector<B>>>mapM(final Function<A, Expression<? extends B>> func) {
		return new Function<Collection<? extends A>, Expression<PVector<B>>>() {
			@Override
			public Expression<PVector<B>> apply(Collection<? extends A> arg) {
				Function<Collection<? extends Expression<? extends B>>, Expression<PVector<B>>> join = joinCollection();
				return join.apply(Collections.mapWith(func).apply(arg));
			}
		};
	}
	
	public static <A> Function<Collection<? extends Expression<? extends A>>, Expression<PVector<A>>> joinCollection(){ 
		return new Function<Collection<? extends Expression<? extends A>>, Expression<PVector<A>>>(){
			@Override
			public Expression<pcollections.PVector<A>> apply(final Collection<? extends org.workcraft.dependencymanager.advanced.core.Expression<? extends A>> collection) {
				return new ExpressionBase<PVector<A>>(){
					@Override
					protected PVector<A> evaluate(EvaluationContext context) {
						PVector<A> res = TreePVector.empty();
						for(Expression<? extends A> ea : collection)
							res =res.plus(context.resolve(ea));
						return res;
					}
				};
			};
		};
	}

	public static <A,B> Function<Expression<? extends A>, Expression<B>> lift(final Function<? super A, ? extends B> func) {
		return new Function<Expression<? extends A>, Expression<B>>(){
			@Override
			public Expression<B> apply(Expression<? extends A> exprA) {
				return bindFunc(exprA, func);
			}
		};
	};
	
	public static <A> Function0<A> asFunction(final Expression<? extends A> expr) {
		return new Function0<A>(){
			@Override
			public A apply() {
				return GlobalCache.eval(expr);
			}
		};
	}

	/**
	 * the actual type is like: (a -> Expression b) -> Expression (a -> IO b)
	 * 
	 * This function leaks memory if the original function constructs a new Expression each time it is called!
	 */
	public static <A, B> Expression<Function<A, B>> joinFunction(final Function<? super A, ? extends Expression<? extends B>> func) {
		return new ExpressionBase<Function<A, B>>() {
			@Override
			protected Function<A, B> evaluate(final EvaluationContext context) {
				return new Function<A, B>() {
					@Override
					public B apply(A a) {
						return context.resolve(func.apply(a));
					}
				};
			}
		};
	}
}
