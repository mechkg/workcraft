package org.workcraft.dependencymanager.advanced.core;

import java.util.ArrayList;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.util.listeners.Listener;


public class GlobalCache {
	//public static CacheManager resolver = new CacheManager();
	public static <T> T eval(IExpression<T> expression) {
		return expression.getValue(null).value;
		//return resolver.eval(expression);
	}
/*	
	public static void changed(Expression<?> expression) {
		resolver.changed(expression);
	}
	
	public static <T> void setValue(ModifiableExpression<? super T> expr, T value) {
		expr.setValue(new DependencyResolver() {
			@Override
			public <T2> T2 resolve(Expression<T2> dependency) {
				return eval(dependency);
			}
		}, value);
	}
	
	public static void onInvalidate(Expression<?> expr, Listener listener) {
		resolver.onInvalidate(expr, listener);
	}*/
	
	static ArrayList<Expression> autoRefreshExpressions = new ArrayList<Expression>();
	static ArrayList<Listener> autoRefreshListeners = new ArrayList<Listener>();
	
	public static void autoRefresh(final Expression<?> expr) {
		
		final Listener[] ls = new Listener[1];
		ls[0] = new Listener() {
			@Override
			public void changed() {
				//System.out.println("autorefresh refreshing...");
				expr.getValue(ls[0]);
			}
		};
		autoRefreshExpressions.add(expr);
		autoRefreshListeners.add(ls[0]);
		expr.getValue(ls[0]);
	}

	public static <T> void setValue(ModifiableExpression<T> expr, T value) {
		expr.setValue(value);
	}
}
