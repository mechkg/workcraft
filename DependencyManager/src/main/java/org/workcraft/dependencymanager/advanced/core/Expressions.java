package org.workcraft.dependencymanager.advanced.core;

import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.ExpressionBase.ValueHandleTuple;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.dependencymanager.advanced.user.PickyModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.PickyModifiableExpressionBase;
import org.workcraft.dependencymanager.advanced.user.Unfold;
import org.workcraft.dependencymanager.util.listeners.Listener;
import org.workcraft.util.Function;
import org.workcraft.util.Function2;

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
	
	public static <T> Expression<T> unfold(Expression<? extends Expression<? extends T>> indirect) {
		return new Unfold<T>(indirect);
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
	
	public static <A,B> Expression<? extends B> bindFunc(final Expression<? extends A> expr1, final Function<? super A, ? extends B> func) {
		notNull(expr1, func);
		return new ExpressionBase<B>(){
			@Override
			protected B evaluate(EvaluationContext context) {
				return func.apply(context.resolve(expr1));
			}
		};
	}
	
	public static <A,B> Expression<? extends B> bind(final Expression<? extends A> expr1, final Combinator<? super A, ? extends B> func) {
		notNull(expr1, func);
		return new ExpressionBase<B>(){
			@Override
			protected B evaluate(EvaluationContext context) {
				return context.resolve(func.apply(context.resolve(expr1)));
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

	public static <A,B,C> Expression<? extends C> bindFunc(final Expression<? extends A> expr1, final Expression<? extends B> expr2, final Function2<? super A, ? super B, ? extends C> func) {
		notNull(expr1, func); 
		return new ExpressionBase<C>(){
			@Override
			protected C evaluate(EvaluationContext context) {
				return func.apply(context.resolve(expr1),context.resolve(expr2));
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
}
